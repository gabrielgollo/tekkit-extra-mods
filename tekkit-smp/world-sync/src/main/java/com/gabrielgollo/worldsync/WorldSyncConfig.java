package com.gabrielgollo.worldsync;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.util.EnumSet;
import java.util.Properties;

final class WorldSyncConfig {
    String endpoint;
    String bucket;
    String region;
    String accessKey;
    String secretKey;
    String userId;
    private final File file;

    private WorldSyncConfig(File file, Properties properties) {
        this.file = file;
        endpoint = properties.getProperty("endpoint", "http://192.168.31.16:9006").trim();
        bucket = properties.getProperty("bucket", "minecraft").trim();
        region = properties.getProperty("region", "us-east-1").trim();
        accessKey = properties.getProperty("accessKey", "").trim();
        secretKey = properties.getProperty("secretKey", "").trim();
        userId = properties.getProperty("userId", "").trim();
    }

    static WorldSyncConfig load(File configDirectory) throws Exception {
        File file = new File(configDirectory, "gabrielgollo-world-sync.properties");
        Properties properties = new Properties();
        if (file.isFile()) {
            FileInputStream input = new FileInputStream(file);
            try {
                properties.load(input);
            } finally {
                input.close();
            }
        }
        boolean created = !file.isFile();
        if (created) {
            properties.setProperty("endpoint", "http://192.168.31.16:9006");
            properties.setProperty("bucket", "minecraft");
            properties.setProperty("region", "us-east-1");
            properties.setProperty("accessKey", "");
            properties.setProperty("secretKey", "");
            properties.setProperty("userId", "");
            File parent = file.getParentFile();
            if (parent != null) parent.mkdirs();
        }
        WorldSyncConfig config = new WorldSyncConfig(file, properties);
        if (created) config.save();
        else configFilePermissions(file);
        return config;
    }

    void update(String endpoint, String bucket, String region, String accessKey, String secretKey, String userId) {
        this.endpoint = endpoint.trim();
        this.bucket = bucket.trim();
        this.region = region.trim();
        this.accessKey = accessKey.trim();
        if (!secretKey.trim().isEmpty()) this.secretKey = secretKey.trim();
        this.userId = userId.trim();
    }

    void save() throws Exception {
        Properties properties = new Properties();
        properties.setProperty("endpoint", endpoint);
        properties.setProperty("bucket", bucket);
        properties.setProperty("region", region);
        properties.setProperty("accessKey", accessKey);
        properties.setProperty("secretKey", secretKey);
        properties.setProperty("userId", userId);
        FileOutputStream output = new FileOutputStream(file);
        try {
            properties.store(output, "World Sync S3-compatible storage. Keep credentials private.");
        } finally {
            output.close();
        }
        configFilePermissions(file);
    }

    private static void configFilePermissions(File file) throws Exception {
        try {
            Files.setPosixFilePermissions(file.toPath(), EnumSet.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE));
        } catch (UnsupportedOperationException ignored) { }
    }

    boolean isConfigured() {
        return !endpoint.isEmpty() && !bucket.isEmpty() && !region.isEmpty()
                && !accessKey.isEmpty() && !secretKey.isEmpty();
    }
}
