# gabrielgollo_Generic Assistant — Tekkit Classic 1.2.5

Development build, compiled against this pack's Minecraft/Forge jars and JInput 2.0.5. Source lives separately from X-Ray Deluxe.

## Default Xbox-style profile

- Left stick: movement; right stick: camera.
- A: jump; B: sneak; X: drop; Y: inventory.
- Triggers: attack/use (combined JInput `z` axis).
- LB/RB: previous/next hotbar slot.
- Start: pause; Back: toggle X-Ray Deluxe if installed.
- Right stick click, F8, or the **Generic Assistant** button on the title screen: settings.
- Menus, including the initial title screen: left stick moves cursor, A left-click, X right-click, B Escape, LB/RB scroll. Menu navigation remains available before the game window has focus so A can focus/start the game.

The device must expose a gamepad, not just keyboard emulation. On the Z1 Extreme handheld select gamepad mode. Device axes differ across drivers: edit `config/generic-assistant.properties` to configure `axis.moveX`, `axis.moveY`, `axis.lookX`, `axis.lookY`, `axis.triggers`, `button.0` through `button.9`, `deadzone`, and `lookSpeed`. Restart after changing the profile file.

## Assistance

Settings menu: automatic tool, automatic jump, automatic treetap, hotbar refill, and broken-tool replacement. Changes save immediately. Automatic jump defaults OFF. Automatic tool only searches the hotbar and compares the real item effectiveness, including modded tools. Treetap selection on use targets only a wet resin face of IC2 rubber wood; normal/electric treetaps are supported when on the hotbar. Dry holes are skipped to avoid damaging resin spots.

## Development status

`./build.sh` creates `dist/gabrielgollo_generic-assistant-1.0.0-mc1.2.5.jar`; `./build.sh --install` also copies it into the pack mods directory. `./checks/run.sh` verifies deadzone, resin rules, and vanilla tool selection under the launcher's Java 8. The settings panel was verified inside Minecraft; real IC2 electric tools and simulated JInput profiles passed checks. See `VALIDATION.md` for target-device validation status. Split-trigger devices are supported through the profile; reconnect detection still depends on the devices exposed by the JInput environment.

### Split trigger profile

For separate trigger axes use `triggerMode=split`, `axis.attack=rz`, `axis.use=z`. Set `triggerRest=-1` for axes whose neutral value is -1, or `triggerRest=0` for axes whose neutral is zero. The defaults remain combined triggers (`triggerMode=combined`, `axis.triggers=z`). Names depend on the actual driver.

Menu clicks wait until A/B/X are released when entering a menu, preventing a held gameplay button from clicking an unrelated menu item.

The settings panel displays the selected device and its analog axis identifiers. `deviceName` optionally filters devices by a case-insensitive substring. The reconnect button retries the devices currently exposed by JInput; a newly connected device may require a game restart with this older input library. Camera control yields to mouse look when the player rotation changes outside the controller, and the right stick takes control again when it is moved.

## Inventory assistance

`refillHotbar` and `replaceBrokenTool` are independent options, enabled by default and available in the settings screen. They work with keyboard/mouse and gamepad input.

When the selected stack is exhausted through use, the assistant moves a matching stack from inventory slots 9–35 into the same hotbar slot. Consumables must match item, variant, and tags. A broken damageable item is replaced by another usable copy of that same item, allowing different durability. Other hotbar slots, armor, offhand, and chests are not searched. Inventory changes use normal Minecraft inventory clicks so the server receives the update.

Opening an inventory, switching worlds or players, changing the selected slot, or dropping an item resets or suppresses replenishment. Existing settings from the previous mod name are copied to the new configuration file on first use if no new configuration exists. Remove the old mod JAR when upgrading to Generic Assistant.

## Controller tuning

Open **Controller settings** in the assistant screen to adjust camera sensitivity (`lookSpeed`, 0.1–30), stick deadzone (`deadzone`, 5–80%), menu cursor speed (`cursorSpeed`, 1–40), and vertical camera inversion (`invertLookY`). Changes apply immediately and are saved automatically. The camera retains its default sensitivity of 5; try 10 for a faster turn rate. Existing profile values are preserved.

The legacy mod list has no Forge configuration factory API; use the title-screen Generic Assistant button or F8 to open these settings.

In menus, the right stick scrolls vertically while the left stick moves the pointer. Push farther to scroll faster; centering the stick stops scrolling. LB/RB scrolling remains available. Scrolling follows the configured `axis.lookY` and is independent of camera inversion.
