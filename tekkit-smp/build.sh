#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")" && pwd)"
INSTALL="${1:-}"
if [ "$INSTALL" != "" ] && [ "$INSTALL" != "--install" ]; then
  echo "Usage: $0 [--install]" >&2
  exit 2
fi

"$ROOT/gradlew" --no-daemon -p "$ROOT/controller-assistant" build
"$ROOT/gradlew" --no-daemon -p "$ROOT/x-ray-deluxe" build

if [ "$INSTALL" = "--install" ]; then
  PACK_ROOT="$(cd "$ROOT/../../technic/modpacks/tekkit-smp" && pwd)"
  mkdir -p "$PACK_ROOT/mods"
  cp "$ROOT/controller-assistant/dist/gabrielgollo_controller-assistant-$(cat "$ROOT/controller-assistant/VERSION")-mc1.12.2.jar" "$PACK_ROOT/mods/"
  cp "$ROOT/x-ray-deluxe/dist/gabrielgollo_x-ray-deluxe-$(cat "$ROOT/x-ray-deluxe/VERSION")-mc1.12.2.jar" "$PACK_ROOT/mods/"
  echo "Tekkit SMP mods installed in $PACK_ROOT/mods"
fi
