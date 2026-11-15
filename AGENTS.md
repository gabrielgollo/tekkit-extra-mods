# Repository Instructions

These instructions apply to all mods in this repository unless a project-level `AGENTS.md` overrides them.

## Preserve the project style

- Keep each mod self-contained in its existing directory. Put Java sources in `src/`, checks in `checks/`, and project documentation beside its build script.
- Follow the existing Java style and legacy ModLoader/Forge APIs used by that mod. Avoid broad refactors or new dependencies when a focused change is sufficient.
- Match the Minecraft version, loader, and Java bytecode target specified by the pack directory. For `tekkit-classic`, this means Minecraft 1.2.5, its Forge Mod Loader, and Java 8 bytecode. Do not use Java language features newer than Java 8 unless a pack explicitly requires a newer runtime.
- Preserve the existing mod IDs/classes, control behavior, configuration formats, and jar naming unless the requested change requires otherwise.
- Keep the root build script and each project's build script working. Generated class files, build directories, and jars belong in ignored `build/` or `dist/` directories, not in source folders.

## Comments and documentation

- Do not generate Javadoc comments (`/** ... */`). Do not add routine explanatory comments to self-explanatory code.
- Add a short regular comment only when needed to explain a non-obvious legacy workaround, side effect, or compatibility constraint. Do not use comments as a substitute for clear names or structure.
- Keep documentation in English. Keep in-game interface text in English by default; provide Portuguese text only through the existing `pt_BR` localization behavior.
- Update the relevant README or validation notes when a change alters build steps, controls, configuration, compatibility, or verified behavior.

## Build and validation

- Use `./build.sh tekkit-classic` at the repository root to build the Classic mods. Add `--install` only when the local pack's `mods/` folder should also be updated.
- For a focused project build, use that mod's own `build.sh` and follow its documented options.
- Keep validation notes factual: distinguish checks that were run from behavior that still needs testing on the target game or controller hardware.
