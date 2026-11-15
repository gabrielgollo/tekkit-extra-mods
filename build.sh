#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")" && pwd)"
PACK="${1:-tekkit-classic}"
INSTALL="${2:-}"

if [ "$INSTALL" != "" ] && [ "$INSTALL" != "--install" ]; then
  echo "Usage: $0 [tekkit-classic|tekkit-smp] [--install]" >&2
  exit 2
fi

case "$PACK" in
  tekkit-classic) "$ROOT/tekkit-classic/build.sh" "$INSTALL" ;;
  tekkit-smp) "$ROOT/tekkit-smp/build.sh" "$INSTALL" ;;
  tekkit-2)
    echo "No buildable ports are available for '$PACK' yet." >&2
    exit 2
    ;;
  *)
    echo "Usage: $0 [tekkit-classic|tekkit-smp] [--install]" >&2
    exit 2
    ;;
esac
