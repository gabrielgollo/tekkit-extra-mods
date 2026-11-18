#!/usr/bin/env bash
set -euo pipefail
project="$(cd "$(dirname "$0")/.." && pwd)"
pack="$project/../../../technic/modpacks/tekkit"
cp="$pack/../../libraries/org/lwjgl/lwjgl/lwjgl/2.9.0/lwjgl-2.9.0.jar:$pack/../../libraries/org/lwjgl/lwjgl/lwjgl_util/2.9.0/lwjgl_util-2.9.0.jar:$project/build:$pack/bin/modpack.jar:$pack/bin/minecraft.jar:$pack/mods/industrialcraft-2-client_1.97.jar:$pack/../../libraries/net/java/jinput/jinput/2.0.5/jinput-2.0.5.jar"
scratch="$(mktemp -d)"
trap 'rm -rf "$scratch"' EXIT
javac -source 8 -target 8 -Xlint:-options -cp "$cp" -d "$scratch" "$project/checks/AssistanceCheck.java" "$project/checks/IC2Check.java" "$project/checks/PadProfileCheck.java" "$project/checks/HotbarCycleCheck.java" "$project/checks/MenuScrollCheck.java" "$project/checks/FrameMotionCheck.java"
"$pack/../../runtimes/jre-legacy/bin/java" -cp "$scratch:$cp" AssistanceCheck

"$pack/../../runtimes/jre-legacy/bin/java" -cp "$scratch:$cp" IC2Check

"$pack/../../runtimes/jre-legacy/bin/java" -cp "$scratch:$cp" PadProfileCheck

"$pack/../../runtimes/jre-legacy/bin/java" -cp "$scratch:$cp" HotbarCycleCheck

"$pack/../../runtimes/jre-legacy/bin/java" -cp "$scratch:$cp" MenuScrollCheck

"$pack/../../runtimes/jre-legacy/bin/java" -cp "$scratch:$cp" FrameMotionCheck
