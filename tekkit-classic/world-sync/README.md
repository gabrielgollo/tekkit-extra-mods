# World Sync for Tekkit Classic

Client-side save synchronization for Tekkit Classic, Minecraft 1.2.5. At startup it checks remote worlds, downloads missing or newer copies, and uploads local saves that are not in the bucket yet. On leaving a single-player world, it uploads the latest save.

Click **World Sync** on the title screen to configure the endpoint, bucket, region, access key, secret key, and sync user ID. Settings are local to this pack in `config/gabrielgollo-world-sync-classic.properties`; credentials are not bundled. Use the same sync user ID on both computers. The object prefix is `tekkit-classic/1.2.5/<sync-user-id>/`.

Normal synchronization runs in the background, including uploads when leaving a world. The configuration screen opens through the title button; conflicts open a prompt for choosing which copy to keep. Allow an ongoing upload to finish before closing Minecraft.

If both copies changed, the screen lets you keep and upload the local world or replace it with the cloud copy. Replaced local saves are retained as `*.world-sync-backup-*` in `saves/`.

## Build

Requires Java 8 and the Tekkit Classic client files at `../technic/modpacks/tekkit`.

```sh
./build.sh tekkit-classic
./build.sh tekkit-classic --install
```

The installed JAR is `gabrielgollo_world-sync-<version>-mc1.2.5.jar`.
