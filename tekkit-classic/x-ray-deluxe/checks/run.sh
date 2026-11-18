#!/usr/bin/env bash
set -euo pipefail
project="$(cd "$(dirname "$0")/.." && pwd)"
pack="$project/../../../technic/modpacks/tekkit"
scratch="$(mktemp -d)"
trap 'rm -rf "$scratch"' EXIT
classpath="$pack/../../libraries/org/lwjgl/lwjgl/lwjgl/2.9.0/lwjgl-2.9.0.jar:$pack/../../libraries/org/lwjgl/lwjgl/lwjgl_util/2.9.0/lwjgl_util-2.9.0.jar:$project/build:$pack/bin/modpack.jar:$pack/bin/minecraft.jar:$pack/mods/buildcraft-client-A-core-2.2.14.zip:$pack/mods/buildcraft-client-B-energy-2.2.14.zip"
javac -source 8 -target 8 -Xlint:-options -d "$scratch" -cp "$classpath" "$project/checks/SelectionCheck.java" "$project/checks/PanelCheck.java" "$project/checks/TextureCheck.java" "$project/checks/OilCheck.java" "$project/checks/DefaultsCheck.java" "$project/checks/TitleMenuCheck.java"
cd "$scratch"
"$pack/../../runtimes/jre-legacy/bin/java" -cp "$scratch:$classpath" SelectionCheck
"$pack/../../runtimes/jre-legacy/bin/java" -cp "$scratch:$classpath" PanelCheck

"$pack/../../runtimes/jre-legacy/bin/java" -cp "$scratch:$classpath" TextureCheck

"$pack/../../runtimes/jre-legacy/bin/java" -cp "$scratch:$classpath" OilCheck

"$pack/../../runtimes/jre-legacy/bin/java" -cp "$scratch:$classpath" DefaultsCheck

"$pack/../../runtimes/jre-legacy/bin/java" -cp "$scratch:$classpath" TitleMenuCheck
