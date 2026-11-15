# Tekkit Extra Mods

Monorepo for client-side mods adapted to individual Tekkit packs. Each pack has its own directory because Minecraft, Forge, and mod APIs differ between pack generations.

## Pack directories

- [`tekkit-classic/`](tekkit-classic/README.md): Minecraft 1.2.5 / Tekkit Classic. Includes Controller Assistant and X-Ray Deluxe.
- [`tekkit-2/`](tekkit-2/README.md): scaffold for future Tekkit 2 ports; no compatible jars are available yet.
- [`tekkit-smp/`](tekkit-smp/README.md): Forge 1.12.2 ports of Controller Assistant and X-Ray Deluxe.

## Build

The local builds expect Tekkit Classic at `../technic/modpacks/tekkit` and Tekkit SMP at `../technic/modpacks/tekkit-smp`; both require JDK 8.

```sh
./build.sh tekkit-classic                 # build both Classic jars into dist/
./build.sh tekkit-classic --install       # build and install into the local pack
./build.sh tekkit-smp                     # build both SMP jars into dist/
./build.sh tekkit-smp --install           # build and install into the local pack
```

Each mod can also be built independently with its own `build.sh`.

## Release versioning

Each pack has an independent release version recorded in its `VERSION` file. Tag names include the pack to avoid collisions, for example `tekkit-classic-v1.0.0`, `tekkit-2-v1.0.0`, and `tekkit-smp-v1.0.0`. Release assets must be built for and named with their target pack and Minecraft version. Never attach a jar from one pack directory to another pack's release.
