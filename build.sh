#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")" && pwd)"
PACK="${1:-tekkit-classic}"
INSTALL="${2:-}"

if [ "$PACK" != "tekkit-classic" ]; then
  echo "No buildable ports are available for '$PACK' yet." >&2
  exit 2
fi
if [ "$INSTALL" != "" ] && [ "$INSTALL" != "--install" ]; then
  echo "Usage: $0 [tekkit-classic] [--install]" >&2
  exit 2
fi

"$ROOT/tekkit-classic/build.sh" "$INSTALL"
