# gabrielgollo_X-Ray Deluxe for Tekkit Classic 1.2.5

Client-side ModLoader/BaseMod mod, compiled against Forge Mod Loader 2.2 from the Tekkit Classic pack.

## Controls

- **X**: toggle X-Ray. Blocks not selected as ores use Minecraft's transparent atlas tile; selected ores remain visible.
- **C**: open the settings panel in-game.
- **X-Ray Deluxe** on the title screen (upper-right corner): configure the mod before opening a world. Escape returns to the title screen.
- Still and flowing lava remain visible while X-Ray is enabled, within the configured range, even when no ores are selected.
- On the **Hidden opacity** row, click the left side to decrease opacity or the right side to increase it in 10% steps from 0% to 100%. The value is saved as `hiddenOpacity` in the configuration.
- Click ore names in the panel to select or deselect them. Selection is saved automatically. Click the status to toggle X-Ray; press **ESC** to close.

Default selection includes vanilla gold, iron, coal, lapis lazuli, diamond, and redstone; Nether Ores; RedPower; and IC2 copper, tin, and uranium. RedPower and Nether Ores are selected by block ID group in this release.

Selected blocks are drawn through walls using their real textures within the range configured in `config/x-ray-deluxe.cfg` (12 blocks by default). The search checks all three axes and respects the world's 0–255 height. Vanilla atlas blocks become transparent; liquids and mod-specific atlases retain their original textures to avoid rendering oil with missing or incorrect textures.

## Build and install

Run `./build.sh` from this directory to create the jar in `dist/`. Use `./build.sh --install` to copy it to the local Tekkit Classic `mods/` directory. From the monorepo root, `./build.sh tekkit-classic --install` builds and installs both Classic mods.

Build requirements: JDK 8 or newer. Output bytecode targets Java 8.

## Default selection JSON

Edit `config/x-ray-deluxe-defaults.json` in the modpack. Its `oreIds` array controls the initial selection and the **Reset defaults** button:

```json
{ "oreIds": [14, 15, 16, 21, 56, 73, 74, 135, 140, 247, 248, 249] }
```

The existing personal selection in `x-ray-deluxe.cfg` is preserved. After editing the JSON, click **Reset defaults** to apply it. An empty array selects no ores; lava remains visible. Invalid JSON falls back to the built-in selection and logs the error. IDs 135 and 140 select all metadata variants in their ore groups. Optional `groups` and `description` fields are documentation only.

Coverage was checked against the Tekkit Classic Wiki and local pack configuration: vanilla coal, iron, gold, diamond, lapis, and redstone; IC2 copper, tin, and uranium; all eight RedPower variants (ruby, emerald, sapphire, silver, tin, copper, tungsten, and nikolite); and all eight installed Nether Ores (coal, diamond, gold, iron, lapis, redstone, copper, and tin). RedPower emerald is a separate pre-1.3 gem, not vanilla emerald ore.

Sources: [Nether Ores](https://tekkitclassic.fandom.com/wiki/Nether_Ores), [Tunnel Bore ore IDs](https://tekkitclassic.fandom.com/wiki/Tunnel_Bore), [Gems](https://tekkitclassic.fandom.com/wiki/Gems).
