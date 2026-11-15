package com.gabrielgollo.worldsync;

import java.io.File;
import java.time.Duration;
import java.net.URI;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;

final class S3WorldStore implements AutoCloseable {
    private final WorldSyncConfig config;
    private final String prefix;
    private final S3Client client;

    S3WorldStore(WorldSyncConfig config, String username) {
        this.config = config;
        this.prefix = "tekkit-smp/1.12.2/" + userSegment(config.userId.isEmpty() ? username : config.userId) + "/worlds/";
        this.client = S3Client.builder()
                .endpointOverride(URI.create(config.endpoint))
                .region(Region.of(config.region))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(config.accessKey, config.secretKey)))
                .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build())
                .overrideConfiguration(builder -> builder
                        .apiCallAttemptTimeout(Duration.ofSeconds(90))
                        .apiCallTimeout(Duration.ofMinutes(5)))
                .httpClientBuilder(UrlConnectionHttpClient.builder()
                        .connectionTimeout(Duration.ofSeconds(15))
                        .socketTimeout(Duration.ofSeconds(60)))
                .build();
    }

    List<RemoteWorld> listWorlds() {
        ArrayList<RemoteWorld> worlds = new ArrayList<RemoteWorld>();
        String token = null;
        do {
            ListObjectsV2Request.Builder request = ListObjectsV2Request.builder().bucket(config.bucket).prefix(prefix);
            if (token != null) request.continuationToken(token);
            ListObjectsV2Response response = client.listObjectsV2(request.build());
            for (S3Object object : response.contents()) {
                String relative = object.key().substring(prefix.length());
                if (!relative.endsWith(".zip")) continue;
                String encoded = relative.substring(0, relative.length() - 4);
                try {
                    String name = new String(Base64.getUrlDecoder().decode(encoded), StandardCharsets.UTF_8);
                    if (!safeWorldName(name)) continue;
                    long savedAt = object.lastModified() == null ? 0L : object.lastModified().toEpochMilli();
                    worlds.add(new RemoteWorld(name, object.key(), savedAt));
                } catch (IllegalArgumentException ignored) { }
            }
            token = response.nextContinuationToken();
        } while (token != null && !token.isEmpty());
        return worlds;
    }

    void upload(File archive, String worldName, long localModified) {
        String key = key(worldName);
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(config.bucket)
                .key(key)
                .contentType("application/zip")
                .metadata(Collections.singletonMap("local-modified", String.valueOf(localModified)))
                .build();
        client.putObject(request, RequestBody.fromFile(archive));
    }

    void download(RemoteWorld world, File archive) throws java.io.IOException {
        Files.deleteIfExists(archive.toPath());
        client.getObject(GetObjectRequest.builder().bucket(config.bucket).key(world.key).build(),
                ResponseTransformer.toFile(archive.toPath()));
    }

    long remoteLocalModified(RemoteWorld world) {
        try {
            String value = client.headObject(HeadObjectRequest.builder().bucket(config.bucket).key(world.key).build())
                    .metadata().get("local-modified");
            return value == null ? world.modified : Long.parseLong(value);
        } catch (NumberFormatException ignored) {
            return world.modified;
        }
    }

    String key(String worldName) {
        return prefix + segment(worldName) + ".zip";
    }

    private static String segment(String value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private static String userSegment(String value) {
        if (value.matches("[A-Za-z0-9._-]{1,64}")) return value;
        return "b64-" + segment(value);
    }

    private static boolean safeWorldName(String name) {
        return !name.isEmpty() && !name.equals(".") && !name.equals("..")
                && name.indexOf('/') < 0 && name.indexOf('\\') < 0 && name.indexOf('\0') < 0;
    }

    @Override
    public void close() {
        client.close();
    }

    static final class RemoteWorld {
        final String name;
        final String key;
        final long modified;
        RemoteWorld(String name, String key, long modified) {
            this.name = name;
            this.key = key;
            this.modified = modified;
        }
    }
}
