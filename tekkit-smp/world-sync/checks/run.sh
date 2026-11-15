#!/usr/bin/env bash
set -euo pipefail
PROJECT="$(cd "$(dirname "$0")/.." && pwd)"
OUTPUT="$PROJECT/build/behavior-check"
mkdir -p "$OUTPUT"
mapfile -t FIXTURES < <(find "$PROJECT/checks/fixtures" -name '*.java')
javac -source 8 -target 8 -d "$OUTPUT" "${FIXTURES[@]}" \
  "$PROJECT/src/main/java/com/gabrielgollo/worldsync/WorldSyncMod.java" \
  "$PROJECT/src/main/java/com/gabrielgollo/worldsync/WorldSyncConfig.java" \
  "$PROJECT/src/main/java/com/gabrielgollo/worldsync/WorldArchive.java"
java -cp "$OUTPUT" com.gabrielgollo.worldsync.BehaviorCheck
