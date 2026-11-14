#!/usr/bin/env bash
set -euo pipefail
project="$(cd "$(dirname "$0")/.." && pwd)"
pack="$project/../../technic/modpacks/tekkit"
cp="$project/build:$pack/bin/modpack.jar:$pack/bin/minecraft.jar:$pack/mods/industrialcraft-2-client_1.97.jar:$pack/../../libraries/net/java/jinput/jinput/2.0.5/jinput-2.0.5.jar"
scratch="$(mktemp -d)"
trap 'rm -rf "$scratch"' EXIT
javac -source 8 -target 8 -Xlint:-options -cp "$cp" -d "$scratch" "$project/checks/AssistanceCheck.java" "$project/checks/IC2Check.java" "$project/checks/PadProfileCheck.java"
"$pack/../../runtimes/jre-legacy/bin/java" -cp "$scratch:$cp" AssistanceCheck

"$pack/../../runtimes/jre-legacy/bin/java" -cp "$scratch:$cp" IC2Check

"$pack/../../runtimes/jre-legacy/bin/java" -cp "$scratch:$cp" PadProfileCheck
