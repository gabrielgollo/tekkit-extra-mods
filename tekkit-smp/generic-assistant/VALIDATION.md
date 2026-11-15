# Validation

## Generic Assistant inventory features

The mod, package names, assets, configuration paths, build scripts, and documentation were renamed to Generic Assistant. Both packs use mod version 1.0.0. Legacy configuration names remain only for migration and old-JAR cleanup.

Classic checks cover matching item variants, replacing a worn pickaxe, excluding other hotbar slots, five setting hit areas, and the existing IC2/gamepad behavior. SMP isolated checks exercise the production hotbar tracker with inventory fixtures: depleted stacks, broken tools, normal inventory swap calls, independent option flags, drop suppression, GUI/cursor/world/slot guards, NBT/variant matching, and unusable tools. Forge compilation validates the real SMP inventory API calls.

Live visual verification and inventory synchronization on a multiplayer server remain unverified.
