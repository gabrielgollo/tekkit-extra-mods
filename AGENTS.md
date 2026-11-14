# Repository Instructions

These instructions apply to both mods in this repository unless a project-level `AGENTS.md` overrides them.

## Preserve the project style

- Keep each mod self-contained in its existing directory. Put Java sources in `src/`, checks in `checks/`, and project documentation beside its build script.
- Follow the existing Java style and legacy ModLoader/Forge APIs used by that mod. Avoid broad refactors or new dependencies when a focused change is sufficient.
- Keep compatibility with Minecraft 1.2.5, the pack's Forge Mod Loader, and Java 8 bytecode. Do not use APIs or language features newer than Java 8.
- Preserve the existing mod IDs/classes, control behavior, configuration formats, and jar naming unless the requested change requires otherwise.
- Keep the root build script and each project's build script working. Generated class files, build directories, and jars belong in ignored `build/` or `dist/` directories, not in source folders.

## Comments and documentation

- Do not generate Javadoc comments (`/** ... */`). Do not add routine explanatory comments to self-explanatory code.
- Add a short regular comment only when needed to explain a non-obvious legacy workaround, side effect, or compatibility constraint. Do not use comments as a substitute for clear names or structure.
- Keep documentation in English. Keep in-game interface text in English by default; provide Portuguese text only through the existing `pt_BR` localization behavior.
- Update the relevant README or validation notes when a change alters build steps, controls, configuration, compatibility, or verified behavior.

## Build and validation

- Use `./build.sh` at the repository root to build both mods. Use `./build.sh --install` only when the pack's `mods/` folder should also be updated.
- For a focused project build, use that mod's own `build.sh` and follow its documented options.
- Keep validation notes factual: distinguish checks that were run from behavior that still needs testing on the target game or controller hardware.
