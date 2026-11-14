# Tekkit Classic Mods

Target: Tekkit Classic, Minecraft 1.2.5, Forge Mod Loader 2.2.

## Mods

- [`controller-assistant/`](controller-assistant/README.md): gamepad menu navigation and gameplay assistance.
- [`x-ray-deluxe/`](x-ray-deluxe/README.md): configurable ore and lava visibility.

## Build

From the monorepo root:

```sh
./build.sh tekkit-classic
./build.sh tekkit-classic --install
```

The independent project scripts are also available in each mod directory. The local build uses the Tekkit Classic jars at `../technic/modpacks/tekkit`.
