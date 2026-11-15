package com.gabrielgollo.worldsync;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;

@Mod(modid = WorldSyncMod.MOD_ID, name = "gabrielgollo_World Sync", version = "1.0.0-mc1.12.2", clientSideOnly = true, acceptedMinecraftVersions = "[1.12.2]", guiFactory = "com.gabrielgollo.worldsync.WorldSyncGuiFactory")
public final class WorldSyncMod {
    static final String MOD_ID = "gabrielgollo_worldsync";
    private static final ExecutorService WORKER = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "gabrielgollo-world-sync");
        thread.setDaemon(true);
        thread.setUncaughtExceptionHandler((worker, error) -> {
            WorldSyncMod current = WorldSyncMod.instance();
            if (current != null) current.fail(error);
        });
        return thread;
    });
    private static WorldSyncMod instance;
    private WorldSyncConfig config;
    private volatile String status = "Ready";
    private volatile boolean busy;
    private boolean serverStopPending;
    private File closingWorld;
    private String closingWorldName;
    private File pendingUploadWorld;
    private String pendingUploadName;
    private List<S3WorldStore.RemoteWorld> remoteWorlds = new ArrayList<S3WorldStore.RemoteWorld>();
    private int nextRemoteIndex;
    private volatile Conflict conflict;
    private boolean startupSyncStarted;
    private volatile boolean conflictPromptShown;
    private boolean localUploadsChecked;
    private File savesDirectory;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        instance = this;
        try {
            config = WorldSyncConfig.load(event.getModConfigurationDirectory());
            MinecraftForge.EVENT_BUS.register(this);
        } catch (Exception | LinkageError error) {
            fail(error);
        }
    }

    @net.minecraftforge.fml.common.eventhandler.SubscribeEvent
    public void onClientTick(net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent event) {
        if (event.phase != net.minecraftforge.fml.common.gameevent.TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (!(mc.currentScreen instanceof GuiMainMenu) || mc.world != null) return;
        if (!startupSyncStarted) {
            startupSyncStarted = true;
            savesDirectory = new File(mc.mcDataDir, "saves");
            retrySync();
        }
        if (conflict != null && !conflictPromptShown) {
            conflictPromptShown = true;
            mc.displayGuiScreen(new WorldSyncScreen(this, mc.currentScreen));
        }
    }

    GuiScreen createSyncScreen(GuiScreen parent) {
        savesDirectory = new File(Minecraft.getMinecraft().mcDataDir, "saves");
        return new WorldSyncScreen(this, parent);
    }

    @Mod.EventHandler
    public void onServerStopping(FMLServerStoppingEvent event) {
        net.minecraft.server.MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        if (server == null || server.isDedicatedServer()) return;
        serverStopPending = true;
        closingWorldName = server.getFolderName();
        closingWorld = new File(new File(Minecraft.getMinecraft().mcDataDir, "saves"), closingWorldName);
        savesDirectory = closingWorld.getParentFile();
    }

    @Mod.EventHandler
    public void onServerStopped(FMLServerStoppedEvent event) {
        if (!serverStopPending || closingWorld == null) return;
        serverStopPending = false;
        startExitUpload(closingWorld, closingWorldName);
        closingWorld = null;
        closingWorldName = null;
    }

    private void beginStartupSync() {
        if (busy || !isConfigured()) return;
        busy = true;
        status = "Checking remote worlds";
        WORKER.execute(() -> {
            try (S3WorldStore store = store()) {
                remoteWorlds = store.listWorlds();
                nextRemoteIndex = 0;
                localUploadsChecked = false;
                processNextRemote(store);
            } catch (Exception | LinkageError error) {
                fail(error);
            }
        });
    }

    private void processNextRemote(S3WorldStore store) throws Exception {
        while (nextRemoteIndex < remoteWorlds.size()) {
            S3WorldStore.RemoteWorld remote = remoteWorlds.get(nextRemoteIndex++);
            File local = new File(savesDirectory, remote.name);
            long remoteTime = store.remoteLocalModified(remote);
            long localTime = WorldArchive.newestModified(local);
            if (!local.exists()) {
                download(store, remote, local);
            } else if (remoteTime > localTime + 2000L) {
                status = "Restoring newer cloud world: " + remote.name;
                download(store, remote, local);
            } else if (localTime > remoteTime + 2000L) {
                conflictPromptShown = false;
                conflict = new Conflict(remote, local, localTime);
                status = "Choose which copy to keep: " + remote.name;
                busy = false;
                return;
            }
        }
        if (!localUploadsChecked) {
            localUploadsChecked = true;
            uploadMissingLocalWorlds(store);
        }
        status = "World sync complete";
        busy = false;
    }

    private void uploadMissingLocalWorlds(S3WorldStore store) throws Exception {
        File[] worlds = savesDirectory.listFiles();
        if (worlds == null) return;
        for (File world : worlds) {
            String name = world.getName();
            if (!world.isDirectory() || !new File(world, "level.dat").isFile()
                    || name.startsWith(".") || name.contains(".world-sync-backup-")) continue;
            boolean exists = false;
            for (S3WorldStore.RemoteWorld remote : remoteWorlds) {
                if (remote.name.equals(name)) { exists = true; break; }
            }
            if (!exists) upload(store, world, name, WorldArchive.newestModified(world));
        }
    }

    private void download(S3WorldStore store, S3WorldStore.RemoteWorld remote, File local) throws Exception {
        status = "Downloading world: " + remote.name;
        File archive = File.createTempFile("gabrielgollo-world-sync-download-", ".zip");
        try {
            store.download(remote, archive);
            WorldArchive.unpack(archive, savesDirectory, remote.name);
        } finally {
            archive.delete();
        }
    }

    private void startExitUpload(File world, String worldName) {
        pendingUploadWorld = world;
        pendingUploadName = worldName;
        if (!isConfigured()) {
            status = "Configure S3 in config/gabrielgollo-world-sync.properties";
            busy = false;
            return;
        }
        busy = true;
        status = "Preparing world upload: " + worldName;
        WORKER.execute(() -> {
            try (S3WorldStore store = store()) {
                long localTime = WorldArchive.newestModified(world);
                S3WorldStore.RemoteWorld remote = findRemote(store, worldName);
                if (remote != null && store.remoteLocalModified(remote) > localTime + 2000L) {
                    conflictPromptShown = false;
                    conflict = new Conflict(remote, world, localTime);
                    status = "Choose which copy to keep: " + worldName;
                    busy = false;
                    return;
                }
                upload(store, world, worldName, localTime);
                pendingUploadWorld = null;
                pendingUploadName = null;
                status = "World uploaded: " + worldName;
                busy = false;
            } catch (Exception | LinkageError error) {
                fail(error);
            }
        });
    }

    private S3WorldStore.RemoteWorld findRemote(S3WorldStore store, String name) {
        for (S3WorldStore.RemoteWorld remote : store.listWorlds()) if (remote.name.equals(name)) return remote;
        return null;
    }

    private void upload(S3WorldStore store, File world, String name, long localTime) throws Exception {
        status = "Zipping world: " + name;
        File archive = WorldArchive.zip(world);
        try {
            status = "Uploading world: " + name;
            store.upload(archive, name, localTime);
        } finally {
            archive.delete();
        }
    }

    void resolveConflict(boolean useLocal) {
        Conflict current = conflict;
        if (current == null || busy) return;
        conflict = null;
        busy = true;
        WORKER.execute(() -> {
            try (S3WorldStore store = store()) {
                if (useLocal) {
                    upload(store, current.local, current.remote.name, current.localTime);
                } else {
                    download(store, current.remote, current.local);
                }
                if (!useLocal && current.local.equals(pendingUploadWorld)) {
                    pendingUploadWorld = null;
                    pendingUploadName = null;
                } else if (useLocal && current.local.equals(pendingUploadWorld)) {
                    pendingUploadWorld = null;
                    pendingUploadName = null;
                }
                processNextRemote(store);
            } catch (Exception | LinkageError error) {
                fail(error);
            }
        });
    }

    void updateConnection(String endpoint, String bucket, String region, String accessKey, String secretKey, String userId) throws Exception {
        config.update(endpoint, bucket, region, accessKey, secretKey, userId);
        config.save();
    }

    void retrySync() {
        if (busy) return;
        if (savesDirectory == null) savesDirectory = new File(Minecraft.getMinecraft().mcDataDir, "saves");
        if (pendingUploadWorld != null) {
            startExitUpload(pendingUploadWorld, pendingUploadName);
            return;
        }
        conflict = null;
        remoteWorlds = new ArrayList<S3WorldStore.RemoteWorld>();
        nextRemoteIndex = 0;
        beginStartupSync();
    }

    private S3WorldStore store() {
        Minecraft mc = Minecraft.getMinecraft();
        return new S3WorldStore(config, mc.getSession().getUsername());
    }

    private boolean isConfigured() {
        if (config != null && config.isConfigured()) return true;
        status = "Set endpoint, bucket, sync user ID and credentials in config/gabrielgollo-world-sync.properties";
        busy = false;
        return false;
    }

    private void fail(Throwable error) {
        status = "Sync failed: " + error.getClass().getSimpleName() + ": " + error.getMessage();
        busy = false;
        System.err.println("[World Sync] " + status);
        error.printStackTrace();
    }

    String status() { return status; }
    boolean isBusy() { return busy; }
    Conflict conflict() { return conflict; }
    WorldSyncConfig config() { return config; }
    static WorldSyncMod instance() { return instance; }

    static final class Conflict {
        final S3WorldStore.RemoteWorld remote;
        final File local;
        final long localTime;
        Conflict(S3WorldStore.RemoteWorld remote, File local, long localTime) {
            this.remote = remote;
            this.local = local;
            this.localTime = localTime;
        }
    }
}
