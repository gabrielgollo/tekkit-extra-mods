# Controller Assistant — Tekkit Classic 1.2.5

Development build, compiled against this pack's Minecraft/Forge jars and JInput 2.0.5. Source lives separately from X-Ray Deluxe.

## Default Xbox-style profile

- Left stick: movement; right stick: camera.
- A: jump; B: sneak; X: drop; Y: inventory.
- Triggers: attack/use (combined JInput `z` axis).
- LB/RB: previous/next hotbar slot.
- Start: pause; Back: toggle X-Ray Deluxe if installed.
- Right stick click, F8, or the **Controller Assistant** button on the title screen: settings.
- Menus, including the initial title screen: left stick moves cursor, A left-click, X right-click, B Escape, LB/RB scroll. Menu navigation remains available before the game window has focus so A can focus/start the game.

The device must expose a gamepad, not just keyboard emulation. On the Z1 Extreme handheld select gamepad mode. Device axes differ across drivers: edit `config/controller-assistant.properties` to configure `axis.moveX`, `axis.moveY`, `axis.lookX`, `axis.lookY`, `axis.triggers`, `button.0` through `button.9`, `deadzone`, and `lookSpeed`. Restart after changing the profile file.

## Assistance

Settings menu: automatic tool, automatic jump, automatic treetap. Changes save immediately. Automatic jump defaults OFF. Automatic tool only searches the hotbar and compares the real item effectiveness, including modded tools. Treetap selection on use targets only a wet resin face of IC2 rubber wood; normal/electric treetaps are supported when on the hotbar. Dry holes are skipped to avoid damaging resin spots.

## Development status

`./build.sh` creates `dist/gabrielgollo_controller-assistant-1.0.0-mc1.2.5.jar`; `./build.sh --install` also copies it into the pack mods directory. `./checks/run.sh` verifies deadzone, resin rules, and vanilla tool selection under the launcher's Java 8. The settings panel was verified inside Minecraft; real IC2 electric tools and simulated JInput profiles passed checks. See `VALIDATION.md` for target-device validation status. Split-trigger devices are supported through the profile; reconnect detection still depends on the devices exposed by the JInput environment.

### Split trigger profile

For separate trigger axes use `triggerMode=split`, `axis.attack=rz`, `axis.use=z`. Set `triggerRest=-1` for axes whose neutral value is -1, or `triggerRest=0` for axes whose neutral is zero. The defaults remain combined triggers (`triggerMode=combined`, `axis.triggers=z`). Names depend on the actual driver.

Menu clicks wait until A/B/X are released when entering a menu, preventing a held gameplay button from clicking an unrelated menu item.

The settings panel displays the selected device and its analog axis identifiers. `deviceName` optionally filters devices by a case-insensitive substring. The reconnect button retries the devices currently exposed by JInput; a newly connected device may require a game restart with this older input library. Camera control yields to mouse look when the player rotation changes outside the controller, and the right stick takes control again when it is moved.
