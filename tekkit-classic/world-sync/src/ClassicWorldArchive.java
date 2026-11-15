import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

final class ClassicWorldArchive {
    private ClassicWorldArchive() {
    }

    static File saveDirectory(Object world) {
        if (world == null) return null;
        Class<?> type = world.getClass();
        while (type != null) {
            for (Field field : type.getDeclaredFields()) {
                if (!field.getType().getName().equals("akp")) continue;
                try {
                    field.setAccessible(true);
                    Object handler = field.get(world);
                    Method worldNameMethod = handler.getClass().getMethod("d");
                    Object worldName = worldNameMethod.invoke(handler);
                    if (!(worldName instanceof String) || ((String)worldName).isEmpty()) return null;
                    File saves = new File("saves").getCanonicalFile();
                    File directory = new File(saves, (String)worldName).getCanonicalFile();
                    if (directory.getParentFile() == null || !directory.getParentFile().equals(saves)
                            || !new File(directory, "level.dat").isFile()) return null;
                    return directory;
                } catch (Throwable ignored) { }
            }
            type = type.getSuperclass();
        }
        return null;
    }

    static long newestModified(File directory) throws IOException {
        if (!directory.isDirectory()) return 0L;
        try (java.util.stream.Stream<Path> paths = Files.walk(directory.toPath())) {
            return paths.filter(Files::isRegularFile)
                    .filter(path -> !path.getFileName().toString().equals("session.lock"))
                    .mapToLong(path -> {
                        try { return Files.getLastModifiedTime(path).toMillis(); }
                        catch (IOException ignored) { return 0L; }
                    }).max().orElse(0L);
        }
    }

    static File zip(File directory) throws IOException {
        File archive = File.createTempFile("gabrielgollo-classic-world-sync-", ".zip");
        Path root = directory.toPath();
        try (ZipOutputStream output = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(archive)))) {
            try (java.util.stream.Stream<Path> paths = Files.walk(root)) {
                paths.filter(Files::isRegularFile).forEach(path -> {
                    if (path.getFileName().toString().equals("session.lock")) return;
                    String entryName = root.relativize(path).toString().replace(File.separatorChar, '/');
                    try {
                        ZipEntry entry = new ZipEntry(entryName);
                        entry.setTime(Files.getLastModifiedTime(path).toMillis());
                        output.putNextEntry(entry);
                        Files.copy(path, output);
                        output.closeEntry();
                    } catch (IOException error) {
                        throw new ArchiveFailure(error);
                    }
                });
            }
        } catch (ArchiveFailure error) {
            archive.delete();
            throw error.getCause();
        } catch (IOException error) {
            archive.delete();
            throw error;
        }
        return archive;
    }

    static void unpack(File archive, File savesDirectory, String worldName) throws IOException {
        File temporary = new File(savesDirectory, ".world-sync-" + System.nanoTime());
        Path root = temporary.toPath().toAbsolutePath().normalize();
        if (!temporary.mkdirs()) throw new IOException("Could not create temporary world directory");
        try (ZipInputStream input = new ZipInputStream(new FileInputStream(archive))) {
            ZipEntry entry;
            byte[] buffer = new byte[32768];
            while ((entry = input.getNextEntry()) != null) {
                if (entry.getName().indexOf('\\') >= 0) throw new IOException("Unsafe path in world archive");
                Path outputPath = root.resolve(entry.getName()).normalize();
                if (!outputPath.startsWith(root)) throw new IOException("Unsafe path in world archive");
                if (entry.isDirectory()) Files.createDirectories(outputPath);
                else {
                    Path parent = outputPath.getParent();
                    if (parent != null) Files.createDirectories(parent);
                    try (FileOutputStream output = new FileOutputStream(outputPath.toFile())) {
                        int count;
                        while ((count = input.read(buffer)) != -1) output.write(buffer, 0, count);
                    }
                    if (entry.getTime() > 0L) outputPath.toFile().setLastModified(entry.getTime());
                }
                input.closeEntry();
            }
        } catch (IOException error) {
            deleteTree(temporary.toPath());
            throw error;
        }

        File destination = new File(savesDirectory, worldName);
        if (destination.exists()) {
            File backup = new File(savesDirectory, worldName + ".world-sync-backup-" + System.currentTimeMillis());
            move(destination.toPath(), backup.toPath());
        }
        move(temporary.toPath(), destination.toPath());
    }

    private static void move(Path source, Path destination) throws IOException {
        try { Files.move(source, destination, StandardCopyOption.ATOMIC_MOVE); }
        catch (IOException unsupported) { Files.move(source, destination); }
    }

    private static void deleteTree(Path root) throws IOException {
        if (!Files.exists(root)) return;
        try (java.util.stream.Stream<Path> paths = Files.walk(root)) {
            paths.sorted(Comparator.reverseOrder()).forEach(path -> {
                try { Files.deleteIfExists(path); }
                catch (IOException error) { throw new ArchiveFailure(error); }
            });
        } catch (ArchiveFailure error) { throw error.getCause(); }
    }

    private static final class ArchiveFailure extends RuntimeException {
        private static final long serialVersionUID = 1L;
        ArchiveFailure(IOException cause) { super(cause); }
        @Override public synchronized IOException getCause() { return (IOException)super.getCause(); }
    }
}
