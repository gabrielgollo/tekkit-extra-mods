#!/usr/bin/env bash
set -euo pipefail
PROJECT="$(cd "$(dirname "$0")/.." && pwd)"
PACK="$(cd "$PROJECT/../../../technic/modpacks/tekkit" && pwd)"
"$PROJECT/build.sh"
CHECK_ROOT="$(mktemp -d)"
CLASSPATH="$PROJECT/build:$PACK/bin/modpack.jar:$PACK/bin/minecraft.jar"
while IFS= read -r dependency; do
  CLASSPATH="$CLASSPATH:$dependency"
done < <(find "$PACK/../../libraries" -name '*.jar')
mkdir -p "$CHECK_ROOT/saves/CycleCheck"
javac -cp "$CLASSPATH" -d "$CHECK_ROOT" "$PROJECT/checks/CycleCheck.java"
cd "$CHECK_ROOT"
java -cp "$CHECK_ROOT:$CLASSPATH" CycleCheck
