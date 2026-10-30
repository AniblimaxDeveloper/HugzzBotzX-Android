#!/data/data/com.termux/files/usr/bin/bash
set -euo pipefail

cd "$(dirname "$0")/.."
command -v gh >/dev/null 2>&1 || { echo "GitHub CLI belum terpasang. Jalankan: pkg install gh"; exit 1; }
gh auth status >/dev/null 2>&1 || { echo "Login GitHub dahulu: gh auth login"; exit 1; }

GH_USER="$(gh api user --jq .login)"
GH_ID="$(gh api user --jq .id)"
REPO="$GH_USER/HugzzBotzX-Android"

# Correct Git identity keys. This is repository-local, not global.
git config --local user.name "$GH_USER"
git config --local user.email "${GH_ID}+${GH_USER}@users.noreply.github.com"
git branch -M main
git add .

if ! git diff --cached --quiet; then
  git commit -m "HugzzBotzX Premium V2"
else
  echo "Tidak ada perubahan baru untuk di-commit."
fi

if ! gh repo view "$REPO" >/dev/null 2>&1; then
  gh repo create HugzzBotzX-Android --public
fi

if git remote get-url origin >/dev/null 2>&1; then
  git remote set-url origin "https://github.com/$REPO.git"
else
  git remote add origin "https://github.com/$REPO.git"
fi

git push -u origin main
echo "Selesai: https://github.com/$REPO"
