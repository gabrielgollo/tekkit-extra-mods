#!/usr/bin/env bash
set -euo pipefail

PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
VERSION="$(cat "$PROJECT_DIR/VERSION")"
PACK_ROOT="$(cd "$PROJECT_DIR/../../../technic/modpacks/tekkit" && pwd)"
BUILD_DIR="$PROJECT_DIR/build"
DIST_DIR="$PROJECT_DIR/dist"
mkdir -p "$BUILD_DIR" "$DIST_DIR"
rm -f "$BUILD_DIR"/*.class
rm -f "$DIST_DIR"/gabrielgollo_x-ray-deluxe-*-mc1.2.5.jar
javac -source 8 -target 8 -Xlint:-options \
  -cp "$PACK_ROOT/bin/modpack.jar:$PACK_ROOT/bin/minecraft.jar:$PACK_ROOT/../../libraries/org/lwjgl/lwjgl/lwjgl/2.9.0/lwjgl-2.9.0.jar" \
  -d "$BUILD_DIR" \
  "$PROJECT_DIR/src/mod_XRayDeluxe.java" \
  "$PROJECT_DIR/src/XRayDeluxeGui.java"
cp "$PROJECT_DIR/src/mcmod.info" "$BUILD_DIR/mcmod.info"
unzip -p "$PACK_ROOT/bin/minecraft.jar" terrain.png > "$BUILD_DIR/xray-hidden.png"
cp "$PROJECT_DIR/x-ray-deluxe-defaults.json" "$BUILD_DIR/x-ray-deluxe-defaults.json"
JAR="$DIST_DIR/gabrielgollo_x-ray-deluxe-$VERSION-mc1.2.5.jar"
jar cf "$JAR" -C "$BUILD_DIR" .
if [ "${1:-}" = "--install" ]; then
  mkdir -p "$PACK_ROOT/mods"
  cp "$JAR" "$PACK_ROOT/mods/"
  rm -f "$PACK_ROOT/mods/x-ray-deluxe-1.0.0-mc1.2.5.jar"
  echo "Instalado: $PACK_ROOT/mods/$(basename "$JAR")"
else
  echo "Gerado: $JAR"
fi
