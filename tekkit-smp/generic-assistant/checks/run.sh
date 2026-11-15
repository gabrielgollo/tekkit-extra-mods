#!/usr/bin/env bash
set -euo pipefail
PROJECT="$(cd "$(dirname "$0")/.." && pwd)"
OUTPUT="$PROJECT/build/hotbar-check"
mkdir -p "$OUTPUT"
mapfile -t FIXTURES < <(find "$PROJECT/checks/fixtures" -name '*.java')
javac -source 8 -target 8 -d "$OUTPUT" "${FIXTURES[@]}" "$PROJECT/checks/HotbarCheck.java" "$PROJECT/src/main/java/com/gabrielgollo/genericassistant/client/HotbarAssistant.java"
java -cp "$OUTPUT" com.gabrielgollo.genericassistant.client.HotbarCheck
