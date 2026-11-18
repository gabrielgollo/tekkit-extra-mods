# Validation and remaining gate

## Verified

- Renamed release artifact: `gabrielgollo_generic-assistant-1.0.0-mc1.2.5.jar`. Prior live input checks predate the rename and inventory features.
- Full pack startup with 44 mods succeeded on the launcher's Java 8.
- Historical settings screenshots predate the renamed screen and were removed to avoid presenting outdated UI.
- `checks/run.sh` passes: GUI option hit areas; stick deadzone; split-trigger normalization; wet/dry IC2 resin-face rules; shovel/pickaxe/axe selection; real IC2 normal/electric treetap and sufficient-energy checks; charged/discharged IC2 drill and chainsaw selection; simulated JInput component reading, button remapping/release, and device diagnostics.
- The IC2 electric tool evaluator avoids calling the drill efficiency method that plays sounds as a side effect.

## Z1E report

The user confirmed that the controller and X-Ray work on the Z1E. A follow-up report said the game still needed a screen click at startup. Menu input was then changed to remain active while the Minecraft window is unfocused, but that specific fix has not yet been reconfirmed on the Z1E. The initial menu focus behavior therefore remains unverified.

The Linux development session cannot read `/dev/input/event*` (root:input, 0660; current user is not in input), and JInput reports zero accessible controllers. No diagnostic mod remains installed.

## Generic Assistant inventory features

The mod, package names, assets, configuration paths, build scripts, and documentation were renamed to Generic Assistant. Both packs use mod version 1.0.0. Legacy configuration names remain only for migration and old-JAR cleanup.

Classic checks use real Minecraft classes with an isolated client to verify depletion, broken-tool replacement, pickup/place inventory clicks without duplication, and GUI/world/slot/drop/option guards. They also cover matching item variants, excluding other hotbar slots, five setting hit areas, and the existing IC2/gamepad behavior. SMP isolated checks exercise the production hotbar tracker with inventory fixtures: depleted stacks, broken tools, normal inventory swap calls, independent option flags, drop suppression, GUI/cursor/world/slot guards, NBT/variant matching, and unusable tools. Forge compilation validates the real SMP inventory API calls.

Live visual verification and inventory synchronization on a multiplayer server remain unverified.

## Controller tuning configuration

Java 8 builds passed for both targets. Classic profile checks verify loading sensitivity, deadzone, cursor speed, inversion, and bounds. The isolated SMP screen check verifies the Forge configuration factory, routing all six adjustment buttons, inversion and reconnect actions, assistance-page navigation, Done/Escape parent navigation, and button bounds at 320×240. Screen tests use fixtures and do not validate OpenGL or physical handheld input. Live feel on the Z1E remains unverified.

## Right-stick menu scrolling

The standalone MenuScroll check passes on both targets: neutral/drift suppression, immediate up/down response, timed repetition independent of callback frequency, immediate reversal, release/reset, faster full-tilt repetition, and invalid-input rejection. Both menu handlers pass the configured vertical look axis to this helper and emit Robot mouse-wheel events only while navigating menus. Physical handheld scrolling remains unverified.

## Frame-based camera and cursor movement

Java 8 builds passed for Classic and SMP. FrameMotion checks passed at 20, 30, 60, 120, 144, and 240 FPS: the same sensitivity produces the same total rotation over one second, camera motion occurs every frame, and menu pointer fractions preserve total cursor travel. Tests also cover stall limiting, reset, neutral pointer residuals, and negative fractional movement. Existing inventory, menu scrolling, configuration, and input checks still pass.

Classic registers a direct FML RENDER tick-start handler; SMP handles RenderTickEvent.START. Gameplay tick handlers no longer modify camera rotation or move the menu pointer. Live controller feel on the Z1E remains unverified.

## Native title configuration and direct menu input

The Java 8 build and Classic checks passed. MenuInputCheck uses actual legacy Minecraft GUI classes with an isolated client to verify direct A/X/B clicks, releases, held-button suppression, rearming after screen changes, the native configuration button opening AssistantGui, non-duplication, coexistence with a World Sync button, and restoration after menu recreation. Menu buttons and the injected configuration button run from the FML render callback; they no longer rely on the legacy ModLoader GUI tick callback or Robot mouse clicks. The physical Z1E confirmation click remains unverified.
