#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")" && pwd)"
INSTALL="${1:-}"
if [ "$INSTALL" != "" ] && [ "$INSTALL" != "--install" ]; then
  echo "Usage: $0 [--install]" >&2
  exit 2
fi

"$ROOT/controller-assistant/build.sh"
"$ROOT/x-ray-deluxe/build.sh"
"$ROOT/world-sync/build.sh"

if [ "$INSTALL" = "--install" ]; then
  PACK_ROOT="$(cd "$ROOT/../../technic/modpacks/tekkit" && pwd)"
  mkdir -p "$PACK_ROOT/mods"
  rm -f "$PACK_ROOT/mods/controller-assistant-0.1.0-mc1.2.5.jar" "$PACK_ROOT/mods/gabrielgollo_controller-assistant-1.0.0-mc1.2.5.jar" "$PACK_ROOT/mods/x-ray-deluxe-1.0.0-mc1.2.5.jar"
  cp "$ROOT/controller-assistant/dist/gabrielgollo_controller-assistant-$(cat "$ROOT/controller-assistant/VERSION")-mc1.2.5.jar" "$PACK_ROOT/mods/"
  cp "$ROOT/x-ray-deluxe/dist/gabrielgollo_x-ray-deluxe-$(cat "$ROOT/x-ray-deluxe/VERSION")-mc1.2.5.jar" "$PACK_ROOT/mods/"
  cp "$ROOT/world-sync/dist/gabrielgollo_world-sync-$(cat "$ROOT/world-sync/VERSION")-mc1.2.5.jar" "$PACK_ROOT/mods/"
  echo "Tekkit Classic mods installed in $PACK_ROOT/mods"
fi
