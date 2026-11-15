# Validation

The stuck-status investigation found an idle executor while the UI still reported checking remote worlds. The original task failure was not available in the game log. The worker now reports linkage failures and uncaught errors, clears the busy flag, and prints the stack trace. An injected SDK linkage failure verified that retry succeeds after the failure. SDK connection/read timeouts are 15/60 seconds, with a 90-second attempt limit and five-minute total API-call limit. A read-only ListObjectsV2 using the rebuilt shaded JAR and local MinIO configuration succeeded and returned one world.

The Java 8 Forge build retains the SDK AWS S3 client and integrated-server stopping/stopped events. The title-menu client tick starts synchronization once, and opening configuration alone does not start another pass.

`bash checks/run.sh` exercises production synchronization, archive, and configuration classes with isolated Minecraft/Forge fixtures and an in-memory S3 adapter. It verifies first-run configuration creation, automatic startup upload, exclusion of backup folders, a single startup pass, two exit uploads containing updated save bytes, no forced configuration screen during normal sync, and a conflict prompt dispatched on the client tick when the cloud copy is newer.

The Forge build succeeded and the resulting JAR was installed in the local Tekkit SMP profile; generated and installed SHA-256 hashes match.

These isolated checks do not prove live Forge event delivery or network authentication. A playthrough with the built JAR and MinIO remains necessary to confirm those integration points.
