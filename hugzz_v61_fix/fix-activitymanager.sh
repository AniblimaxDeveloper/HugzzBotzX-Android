#!/data/data/com.termux/files/usr/bin/bash
set -euo pipefail
FILE="app/src/main/java/com/hugzzbotzx/app/MainActivity.java"
if [[ ! -f "$FILE" ]]; then
  echo "ERROR: Run this script from the HugzzBotzX repository root. Missing: $FILE" >&2
  exit 1
fi
if grep -q '^import android\.app\.ActivityManager;' "$FILE"; then
  echo "ActivityManager import already present; no change needed."
else
  if ! grep -q '^import android\.app\.Activity;' "$FILE"; then
    echo "ERROR: Expected import anchor not found in $FILE" >&2
    exit 1
  fi
  sed -i '/^import android\.app\.Activity;$/a import android.app.ActivityManager;' "$FILE"
  echo "Added: import android.app.ActivityManager;"
fi
grep -n '^import android\.app\.Activity' "$FILE"
