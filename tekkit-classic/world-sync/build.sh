#!/usr/bin/env bash
set -euo pipefail

PROJECT="$(cd "$(dirname "$0")" && pwd)"
PACK="$(cd "$PROJECT/../../../technic/modpacks/tekkit" && pwd)"
VERSION="$(cat "$PROJECT/VERSION")"
mkdir -p "$PROJECT/build" "$PROJECT/dist"
rm -f "$PROJECT/build"/*.class "$PROJECT/dist"/gabrielgollo_world-sync-*-mc1.2.5.jar
LWJGL="$PACK/../../libraries/org/lwjgl/lwjgl/lwjgl/2.9.0/lwjgl-2.9.0.jar"
javac -source 8 -target 8 -Xlint:-options \
  -cp "$PACK/bin/modpack.jar:$PACK/bin/minecraft.jar:$LWJGL" \
  -d "$PROJECT/build" \
  "$PROJECT/src/"*.java
cp "$PROJECT/src/mcmod.info" "$PROJECT/build/mcmod.info"
JAR="$PROJECT/dist/gabrielgollo_world-sync-$VERSION-mc1.2.5.jar"
jar cf "$JAR" -C "$PROJECT/build" .

if [ "${1:-}" = "--install" ]; then
  mkdir -p "$PACK/mods"
  cp "$JAR" "$PACK/mods/"
  echo "Installed: $PACK/mods/$(basename "$JAR")"
else
  echo "Built: $JAR"
fi
