# Validation Evidence

- Build: `./build.sh` compiles against the local Tekkit jars and creates the jar in `dist/`. Use `./build.sh --install` to copy it into the pack's `mods/` folder.
- `checks/run.sh`: panel click areas, opacity limits (0–100%), search range, and default restoration passed under the launcher's Java 8.
- The jar was checked for the helper atlas `xray-hidden.png` and the world-render handler.
- The isolated `SelectionCheck`, run outside the modpack, passed when deselecting and reselecting redstone (IDs 73 and 74 together), keeping another ore selected, and saving/reloading an empty selection.
- The installed Forge signature `forge.IRenderWorldLastHandler.onRenderWorldLast(l, float)` was confirmed.
- The valid world height range, 0–255, was confirmed against the Minecraft 1.2.5 `World` implementation.

A framebuffer capture from a separate world launched with the full pack, `evidence/minerio-texturizado.png`, shows diamond with its real texture and transparent terrain. The temporary diagnostic code was removed after the capture. Isolated OpenGL checks verified blending at 0%, 10%, 50%, and 100%. Checks using the actual stationary and flowing oil classes verified that their tiles and atlases are preserved at each opacity level.

Current limitation: blocks with their own atlases and liquids keep their original textures; buried ores are drawn with their real textures. RedPower and Nether Ores are still selected by block ID groups, not by metadata variant.

Additional integrated capture, `evidence/opacidade-30-petroleo.png`, shows stone at 30% opacity, diamond with its real texture, and black oil. It was captured in a separate diagnostic world with the full pack. The temporary diagnostic jar was removed.

## Title-screen configuration

Java 8 build and checks passed. TitleMenuCheck verifies the native X-Ray button without a world/player, opening the existing settings screen, editing range before entering a world, Escape returning to the parent menu, duplicate prevention, coexistence with Generic Assistant and World Sync buttons, and button alignment after menu recreation at different dimensions. The button is maintained by a direct FML render callback and needs neither companion mod. Live visual/controller verification remains pending.
