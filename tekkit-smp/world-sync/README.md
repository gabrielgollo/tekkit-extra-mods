# World Sync

Client-side save synchronization for Tekkit SMP 1.12.2. It uploads a zipped single-player world to a private S3-compatible bucket after the integrated server stops. Remote saves are checked automatically in the background when the title screen first opens. Local worlds absent from the bucket are uploaded during the same pass.

The mod uses the AWS SDK for Java 2.x with path-style S3 requests, so it can connect to MinIO. It does not bundle access keys. Click **World Sync** on the title screen to edit the endpoint, bucket, region, access key, secret key, and sync user ID. The settings are saved to `config/gabrielgollo-world-sync.properties` on that computer and must not be committed. The default object prefix is `tekkit-smp/1.12.2/<Minecraft username>/`; set the sync user ID to the same stable value on both computers.

Example local configuration:

```properties
endpoint=http://192.168.31.16:9006
bucket=minecraft
region=us-east-1
accessKey=
secretKey=
userId=GabrielGollo
```

Use the **World Sync** button on the title screen to check the cloud and restore missing or older local worlds. If the local world is newer, the screen asks whether to upload the local copy or replace it with the cloud copy. Replaced local folders are retained as `*.world-sync-backup-*` inside `saves/`.

## Build

Requires JDK 8 and the Tekkit SMP pack at `../technic/modpacks/tekkit-smp`.

```sh
./gradlew --no-daemon -p world-sync build
```

## Sync behavior

- At startup, download newer remote saves, restore missing local worlds, and upload local worlds missing from the bucket.
- If the local world is newer, ask whether to upload the local copy or replace it with the remote copy.
- After the integrated server stops, zip and upload the current world in the background without opening settings.
- Open settings manually to inspect status or save connection settings and retry synchronization.
- A conflict requiring a choice can open the sync screen automatically from the title menu.
- Keep the bucket private and grant the configured credentials access only to the world-sync objects.
