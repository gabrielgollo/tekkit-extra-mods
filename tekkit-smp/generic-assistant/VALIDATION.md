# Validation

## Generic Assistant inventory features

The mod, package names, assets, configuration paths, build scripts, and documentation were renamed to Generic Assistant. Both packs use mod version 1.0.0. Legacy configuration names remain only for migration and old-JAR cleanup.

Classic checks cover matching item variants, replacing a worn pickaxe, excluding other hotbar slots, five setting hit areas, and the existing IC2/gamepad behavior. SMP isolated checks exercise the production hotbar tracker with inventory fixtures: depleted stacks, broken tools, normal inventory swap calls, independent option flags, drop suppression, GUI/cursor/world/slot guards, NBT/variant matching, and unusable tools. Forge compilation validates the real SMP inventory API calls.

Live visual verification and inventory synchronization on a multiplayer server remain unverified.

## Controller tuning configuration

Java 8 builds passed for both targets. Classic profile checks verify loading sensitivity, deadzone, cursor speed, inversion, and bounds. The isolated SMP screen check verifies the Forge configuration factory, routing all six adjustment buttons, inversion and reconnect actions, assistance-page navigation, Done/Escape parent navigation, and button bounds at 320×240. Screen tests use fixtures and do not validate OpenGL or physical handheld input. Live feel on the Z1E remains unverified.

The controller-page regression test dispatches a click by iterating the current button list, matching the vanilla GUI dispatch behavior. It fails against the previous screen with `Missing button 10` and passes with deferred page rebuilding in `updateScreen`. Inversion updates its label without replacing the active button list.

## Right-stick menu scrolling

The standalone MenuScroll check passes on both targets: neutral/drift suppression, immediate up/down response, timed repetition independent of callback frequency, immediate reversal, release/reset, faster full-tilt repetition, and invalid-input rejection. Both menu handlers pass the configured vertical look axis to this helper and emit Robot mouse-wheel events only while navigating menus. Physical handheld scrolling remains unverified.

## Frame-based camera and cursor movement

Java 8 builds passed for Classic and SMP. FrameMotion checks passed at 20, 30, 60, 120, 144, and 240 FPS: the same sensitivity produces the same total rotation over one second, camera motion occurs every frame, and menu pointer fractions preserve total cursor travel. Tests also cover stall limiting, reset, neutral pointer residuals, and negative fractional movement. Existing inventory, menu scrolling, configuration, and input checks still pass.

Classic registers a direct FML RENDER tick-start handler; SMP handles RenderTickEvent.START. Gameplay tick handlers no longer modify camera rotation or move the menu pointer. Live controller feel on the Z1E remains unverified.
