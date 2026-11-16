#!/usr/bin/env bash
set -euo pipefail
PROJECT="$(cd "$(dirname "$0")/.." && pwd)"
OUTPUT="$PROJECT/build/hotbar-check"
mkdir -p "$OUTPUT"
mapfile -t FIXTURES < <(find "$PROJECT/checks/fixtures" -name '*.java')
javac -source 8 -target 8 -d "$OUTPUT" "${FIXTURES[@]}" "$PROJECT/checks/HotbarCheck.java" "$PROJECT/src/main/java/com/gabrielgollo/genericassistant/client/HotbarAssistant.java"
java -cp "$OUTPUT" com.gabrielgollo.genericassistant.client.HotbarCheck
UI_OUTPUT="$PROJECT/build/camera-ui-check"
mkdir -p "$UI_OUTPUT"
mapfile -t UI_FIXTURES < <(find "$PROJECT/checks/ui-fixtures" -name '*.java')
javac -source 8 -target 8 -d "$UI_OUTPUT" "${UI_FIXTURES[@]}" "$PROJECT/checks/CameraScreenCheck.java" "$PROJECT/src/main/java/com/gabrielgollo/genericassistant/client/AssistantSettingsScreen.java" "$PROJECT/src/main/java/com/gabrielgollo/genericassistant/client/AssistantGuiFactory.java"
java -cp "$UI_OUTPUT" com.gabrielgollo.genericassistant.client.CameraScreenCheck

javac -source 8 -target 8 -d "$OUTPUT" "$PROJECT/checks/MenuScrollCheck.java" "$PROJECT/src/main/java/com/gabrielgollo/genericassistant/client/MenuScroll.java"
java -cp "$OUTPUT" com.gabrielgollo.genericassistant.client.MenuScrollCheck
