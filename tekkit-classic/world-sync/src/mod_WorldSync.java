import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import net.minecraft.client.Minecraft;

public final class mod_WorldSync extends BaseMod {
    private static final ExecutorService WORKER = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "gabrielgollo-classic-world-sync");
        thread.setDaemon(true);
        return thread;
    });
    private ClassicWorldSyncConfig config;
    private volatile String status = "Ready";
    private volatile boolean busy;
    private volatile Conflict conflict;
    private volatile List<ClassicS3WorldStore.RemoteWorld> remoteWorlds = new ArrayList<ClassicS3WorldStore.RemoteWorld>();
    private int nextRemoteIndex;
    private boolean localUploadsChecked;
    private File savesDirectory = new File("saves");
    private File activeWorld;
    private File pendingUploadWorld;
    private String pendingUploadName;
    private boolean startupScreenShown;
    private boolean startupSyncStarted;
    private boolean conflictPromptShown;

    public String getVersion() { return "1.0.0-mc1.2.5"; }
    public String getName() { return "gabrielgollo_World Sync Classic"; }

    public void load() {
        try { config = ClassicWorldSyncConfig.load(); }
        catch (Exception error) { fail(error); }
        cpw.mods.fml.common.FMLCommonHandler.instance().registerTickHandler(new ClassicWorldSyncTicker(this));
    }

    public boolean onTickInGame(float partial, Minecraft mc) {
        if (mc != null && mc.f != null) {
            File directory = ClassicWorldArchive.saveDirectory(mc.f);
            if (directory != null) {
                if (activeWorld == null || !activeWorld.equals(directory)) {
                    ModLoader.getLogger().info("[World Sync] Tracking save " + directory.getAbsolutePath());
                }
                activeWorld = directory;
                savesDirectory = directory.getParentFile();
            }
        }
        return true;
    }

    public boolean onTickInGUI(float partial, Minecraft mc, vp gui) {
        if (mc == null || gui == null) return true;
        if (gui instanceof xt) {
            if (activeWorld != null && activeWorld.isDirectory()) {
                pendingUploadName = activeWorld.getName();
                ModLoader.getLogger().info("[World Sync] World closed; starting upload for " + pendingUploadName);
                startExitUpload(activeWorld, pendingUploadName);
                activeWorld = null;
            }
            if (!(gui instanceof ClassicWorldSyncMainMenu)) {
                mc.a(new ClassicWorldSyncMainMenu(this));
                return true;
            }
            if (!startupScreenShown) {
                startupScreenShown = true;
                if (!startupSyncStarted) {
                    startupSyncStarted = true;
                    retrySync();
                }
            }
            if (conflict != null && !conflictPromptShown) {
                conflictPromptShown = true;
                mc.a(new ClassicWorldSyncGui(this, false));
                return true;
            }
        }
        return true;
    }

    void retrySync() {
        if (busy) return;
        if (pendingUploadWorld != null && pendingUploadWorld.isDirectory()) {
            startExitUpload(pendingUploadWorld, pendingUploadName);
            return;
        }
        if (!configured()) return;
        conflict = null;
        remoteWorlds = new ArrayList<ClassicS3WorldStore.RemoteWorld>();
        nextRemoteIndex = 0;
        localUploadsChecked = false;
        busy = true;
        status = "Checking remote worlds";
        WORKER.execute(() -> {
            try {
                ClassicS3WorldStore store = store();
                remoteWorlds = store.listWorlds();
                processNextRemote(store);
            } catch (Exception error) { fail(error); }
        });
    }

    private void processNextRemote(ClassicS3WorldStore store) throws Exception {
        while (nextRemoteIndex < remoteWorlds.size()) {
            ClassicS3WorldStore.RemoteWorld remote = remoteWorlds.get(nextRemoteIndex++);
            File local = new File(savesDirectory, remote.name);
            long remoteTime = store.remoteLocalModified(remote);
            long localTime = ClassicWorldArchive.newestModified(local);
            if (!local.exists() || remoteTime > localTime + 2000L) {
                status = "Downloading world: " + remote.name;
                File archive = File.createTempFile("gabrielgollo-world-sync-", ".zip");
                try {
                    store.download(remote, archive);
                    ClassicWorldArchive.unpack(archive, savesDirectory, remote.name);
                } finally { archive.delete(); }
            } else if (localTime > remoteTime + 2000L) {
                conflict = new Conflict(remote, local, localTime);
                status = "Choose which copy to keep: " + remote.name;
                busy = false;
                return;
            }
        }
        if (!localUploadsChecked) {
            localUploadsChecked = true;
            uploadLocalWorldsMissingRemotely(store);
        }
        status = "World sync complete";
        busy = false;
    }

    private void uploadLocalWorldsMissingRemotely(ClassicS3WorldStore store) throws Exception {
        File[] entries = savesDirectory.listFiles();
        if (entries == null) return;
        for (File world : entries) {
            if (!world.isDirectory() || !new File(world, "level.dat").isFile()) continue;
            String name = world.getName();
            if (name.startsWith(".") || name.contains(".world-sync-backup-")) continue;
            boolean existsRemotely = false;
            for (ClassicS3WorldStore.RemoteWorld remote : remoteWorlds) {
                if (remote.name.equals(name)) { existsRemotely = true; break; }
            }
            if (existsRemotely) continue;
            long modified = ClassicWorldArchive.newestModified(world);
            if (modified == 0L) continue;
            status = "Uploading local world: " + name;
            ModLoader.getLogger().info("[World Sync] Uploading local world absent from S3: " + name);
            upload(store, world, name, modified);
            remoteWorlds.add(new ClassicS3WorldStore.RemoteWorld(name, "", modified));
            ModLoader.getLogger().info("[World Sync] Initial upload complete for " + name);
        }
    }

    private void startExitUpload(File world, String name) {
        pendingUploadWorld = world;
        pendingUploadName = name;
        if (!configured()) { status = "Set MinIO credentials, then press Save and Sync"; return; }
        busy = true;
        status = "Preparing world upload: " + name;
        WORKER.execute(() -> {
            try {
                ClassicS3WorldStore store = store();
                long localTime = ClassicWorldArchive.newestModified(world);
                ClassicS3WorldStore.RemoteWorld remote = findRemote(store, name);
                if (remote != null && store.remoteLocalModified(remote) > localTime + 2000L) {
                    conflictPromptShown = false;
                    conflict = new Conflict(remote, world, localTime);
                    status = "Choose which copy to keep: " + name;
                    busy = false;
                    return;
                }
                upload(store, world, name, localTime);
                pendingUploadWorld = null;
                pendingUploadName = null;
                activeWorld = null;
                status = "World uploaded: " + name;
                ModLoader.getLogger().info("[World Sync] Upload complete for " + name);
                busy = false;
            } catch (Exception error) { fail(error); }
        });
    }

    private ClassicS3WorldStore.RemoteWorld findRemote(ClassicS3WorldStore store, String name) throws Exception {
        for (ClassicS3WorldStore.RemoteWorld remote : store.listWorlds()) if (remote.name.equals(name)) return remote;
        return null;
    }

    private void upload(ClassicS3WorldStore store, File world, String name, long modified) throws Exception {
        status = "Zipping world: " + name;
        File archive = ClassicWorldArchive.zip(world);
        try {
            status = "Uploading world: " + name;
            store.upload(archive, name, modified);
        } finally { archive.delete(); }
    }

    void resolveConflict(boolean useLocal) {
        Conflict current = conflict;
        if (current == null || busy) return;
        conflict = null;
        busy = true;
        WORKER.execute(() -> {
            try {
                ClassicS3WorldStore store = store();
                if (useLocal) upload(store, current.local, current.remote.name, current.localTime);
                else {
                    File archive = File.createTempFile("gabrielgollo-world-sync-", ".zip");
                    try {
                        store.download(current.remote, archive);
                        ClassicWorldArchive.unpack(archive, savesDirectory, current.remote.name);
                    } finally { archive.delete(); }
                }
                pendingUploadName = null;
                pendingUploadWorld = null;
                activeWorld = null;
                processNextRemote(store);
            } catch (Exception error) { fail(error); }
        });
    }

    void updateConnection(String endpoint, String bucket, String region, String accessKey, String secretKey, String userId) throws Exception {
        config.update(endpoint, bucket, region, accessKey, secretKey, userId);
        config.save();
    }

    private boolean configured() {
        if (config != null && config.isConfigured()) return true;
        status = "Set bucket and MinIO credentials in config/gabrielgollo-world-sync-classic.properties";
        busy = false;
        return false;
    }

    private ClassicS3WorldStore store() throws Exception {
        String username = config == null || config.userId.isEmpty() ? "player" : config.userId;
        return new ClassicS3WorldStore(config, username);
    }

    private void fail(Exception error) {
        status = "Sync failed: " + error.getClass().getSimpleName() + ": " + error.getMessage();
        busy = false;
        System.err.println("[World Sync] " + status);
    }

    String status() { return status; }
    boolean isBusy() { return busy; }
    Conflict conflict() { return conflict; }
    ClassicWorldSyncConfig config() { return config; }
    void returnToTitle() { ModLoader.getMinecraftInstance().a(new xt()); }

    static final class Conflict {
        final ClassicS3WorldStore.RemoteWorld remote;
        final File local;
        final long localTime;
        Conflict(ClassicS3WorldStore.RemoteWorld remote, File local, long localTime) {
            this.remote = remote;
            this.local = local;
            this.localTime = localTime;
        }
    }
}
