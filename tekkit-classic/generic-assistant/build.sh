#!/usr/bin/env bash
set -euo pipefail
project="$(cd "$(dirname "$0")" && pwd)"
version="$(cat "$project/VERSION")"
pack="$project/../../../technic/modpacks/tekkit"
mkdir -p "$project/build" "$project/dist"
rm -f "$project/dist"/gabrielgollo_generic-assistant-*-mc1.2.5.jar
javac -source 8 -target 8 -Xlint:-options -cp "$pack/bin/modpack.jar:$pack/bin/minecraft.jar:$pack/../../libraries/org/lwjgl/lwjgl/lwjgl/2.9.0/lwjgl-2.9.0.jar:$pack/../../libraries/net/java/jinput/jinput/2.0.5/jinput-2.0.5.jar" -d "$project/build" "$project/src/"*.java
cp "$project/src/mcmod.info" "$project/build/mcmod.info"
jar cf "$project/dist/gabrielgollo_generic-assistant-$version-mc1.2.5.jar" -C "$project/build" .

if [ "${1:-}" = "--install" ]; then
  find "$pack/mods" -maxdepth 1 -type f \( -name "gabrielgollo_controller-assistant-*.jar" -o -name "controller-assistant-*.jar" \) -delete
  cp "$project/dist/gabrielgollo_generic-assistant-$version-mc1.2.5.jar" "$pack/mods/"
fi
