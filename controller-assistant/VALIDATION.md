# Validation and remaining gate

## Verified

- Jar installed at `technic/modpacks/tekkit/mods/gabrielgollo_controller-assistant-0.1.0-mc1.2.5.jar`.
- Full pack startup with 44 mods succeeded on the launcher's Java 8.
- Settings panel captured from Minecraft framebuffer: `evidence/settings-en.png`.
- `checks/run.sh` passes: GUI option hit areas; stick deadzone; split-trigger normalization; wet/dry IC2 resin-face rules; shovel/pickaxe/axe selection; real IC2 normal/electric treetap and sufficient-energy checks; charged/discharged IC2 drill and chainsaw selection; simulated JInput component reading, button remapping/release, and device diagnostics.
- The IC2 electric tool evaluator avoids calling the drill efficiency method that plays sounds as a side effect.

## Required physical validation

The current Linux session cannot read `/dev/input/event*` (root:input, 0660; current user is not in input), and JInput reports zero accessible controllers. This does not establish the behavior of the Z1E Windows driver.

On the Z1E: start in gamepad mode, open F8, confirm controller name and axes. Verify movement/camera, both triggers, A/B/X/Y, slot shoulders, Start/Back, settings on right-stick click, menu pointer and clicks, automatic jump at a one-block obstacle, IC2 resin extraction, and command release when disconnecting or switching focus. Adjust combined/split trigger mode and axis names in the profile if necessary.

Until those checks are performed, physical controller operation and driver-specific bindings remain unverified. No diagnostic mod remains installed.
