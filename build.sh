#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")" && pwd)"
INSTALL=false
if [ "${1:-}" = "--install" ]; then
  INSTALL=true
elif [ "$#" -gt 0 ]; then
  echo "Uso: $0 [--install]" >&2
  exit 2
fi

"$ROOT/controller-assistant/build.sh"
"$ROOT/x-ray-deluxe/build.sh"

if [ "$INSTALL" = true ]; then
  PACK_ROOT="$(cd "$ROOT/../technic/modpacks/tekkit" && pwd)"
  mkdir -p "$PACK_ROOT/mods"
  cp "$ROOT/controller-assistant/dist/gabrielgollo_controller-assistant-0.1.0-mc1.2.5.jar" "$PACK_ROOT/mods/"
  cp "$ROOT/x-ray-deluxe/dist/gabrielgollo_x-ray-deluxe-1.0.0-mc1.2.5.jar" "$PACK_ROOT/mods/"
  rm -f "$PACK_ROOT/mods/controller-assistant-0.1.0-mc1.2.5.jar" "$PACK_ROOT/mods/x-ray-deluxe-1.0.0-mc1.2.5.jar"
  echo "Mods instalados em $PACK_ROOT/mods"
fi
