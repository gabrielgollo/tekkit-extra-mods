# Validation and remaining gate

## Verified

- Jar installed at `technic/modpacks/tekkit/mods/gabrielgollo_controller-assistant-0.1.0-mc1.2.5.jar`.
- Full pack startup with 44 mods succeeded on the launcher's Java 8.
- Settings panel captured from Minecraft framebuffer: `evidence/settings-en.png`.
- `checks/run.sh` passes: GUI option hit areas; stick deadzone; split-trigger normalization; wet/dry IC2 resin-face rules; shovel/pickaxe/axe selection; real IC2 normal/electric treetap and sufficient-energy checks; charged/discharged IC2 drill and chainsaw selection; simulated JInput component reading, button remapping/release, and device diagnostics.
- The IC2 electric tool evaluator avoids calling the drill efficiency method that plays sounds as a side effect.

## Z1E report

The user confirmed that the controller and X-Ray work on the Z1E. A follow-up report said the game still needed a screen click at startup. Menu input was then changed to remain active while the Minecraft window is unfocused, but that specific fix has not yet been reconfirmed on the Z1E. The initial menu focus behavior therefore remains unverified.

The Linux development session cannot read `/dev/input/event*` (root:input, 0660; current user is not in input), and JInput reports zero accessible controllers. No diagnostic mod remains installed.
