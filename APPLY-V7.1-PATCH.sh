#!/data/data/com.termux/files/usr/bin/bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")" && pwd)"
cd "$ROOT"
if [ ! -d .git ]; then echo "Run this inside the existing HugzzBotzX git repository." >&2; exit 1; fi
echo "V7.1 source extracted. Review with: git status --short"
