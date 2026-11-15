# Validation

## Confirmed

- The user confirmed that synchronization and the restored title button worked in the live game. Their follow-up requested background uploads instead of automatically opening the configuration screen.
- After that adjustment, two isolated entry/exit checks confirmed that the title menu stays active during upload and that each ZIP contains the latest local save. The rebuilt JAR was installed and its SHA-256 matches the generated artifact.
- Live Minecraft execution on 2026-11-15 recorded `Tracking save` for `New World` at 18:39:02 (game log time), followed by `World closed; starting upload` and `Upload complete` at 18:39:52. A subsequent read-only download from MinIO confirmed that the remote ZIP's `level.dat` matched the current local save byte for byte. Remote local-modified metadata was `1794775192000`.
- `bash checks/run.sh` passed two consecutive entry/exit cycles using actual Minecraft/FML classes, an uninitialized windowless client, and an isolated HTTP receiver. Both cycles detected the save, uploaded a ZIP containing the latest changed `level.dat`, and restored `ClassicWorldSyncMainMenu`. This check invokes the ticker directly; it does not validate OpenGL drawing or live FML scheduling.
- Java 8 compilation and installation in the local Tekkit Classic profile succeeded.
- The installed JAR matches the generated artifact by SHA-256.
- A signed upload of `New World` succeeded and the object appeared in the Classic prefix.
- The legacy FML `onRenderTickEnd` dispatches `TickType.RENDER` independently of the loaded world. World Sync now registers a direct `ITickHandler` for this event rather than using the ModLoader clock-dependent GUI ticker.
- The save handler's `d()` method returns the save folder name. Its `a(String)` method returns a file in the world's `data` directory and must not be used to locate `level.dat`.

## Repeating the runtime check

Start Minecraft with the latest installed JAR, enter a single-player world, make a persistent change, and return to the title screen. Verify these log entries in order:

1. `[World Sync] Tracking save ...`
2. `[World Sync] World closed; starting upload for ...`
3. `[World Sync] Upload complete for ...`

Confirm the remote ZIP contains the changed save and that the native World Sync button remains visible and opens its configuration screen. Repeat the cycle to verify the callback remains registered.

The live upload and title button were confirmed after the direct ticker correction. The later background-upload adjustment passed the isolated cycle check; it has not had a separate visual playthrough.
