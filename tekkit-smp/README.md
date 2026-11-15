# Tekkit SMP Mods

Target: Tekkit SMP, Minecraft 1.12.2, Forge 14.23.5.2860. These ports are separate from the Minecraft 1.2.5 Tekkit Classic builds.

Pack release: `1.0.0`.

## Mods

- [`controller-assistant/`](controller-assistant/README.md): controller input, menu navigation, and gameplay assistance.
- [`x-ray-deluxe/`](x-ray-deluxe/README.md): configurable ore highlighting and X-Ray rendering.
- [`world-sync/`](world-sync/README.md): synchronize single-player saves to a private S3-compatible bucket.

## Build

Requires JDK 8 and the Tekkit SMP files at `../technic/modpacks/tekkit-smp`.

```sh
./build.sh tekkit-smp
./build.sh tekkit-smp --install
```

MixinBooter is required by X-Ray Deluxe for block rendering; it is included in the Tekkit SMP pack.
