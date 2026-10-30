# Termux build / update guide

Download `HugzzBotzX-Family-V3.0.zip` to Android Downloads. This archive contains a top-level `HugzzBotzX/` folder. Extract it over your project folder so the existing `.git` folder remains intact.

```bash
termux-setup-storage
cd ~/projects
unzip -o /sdcard/Download/HugzzBotzX-Family-V3.0.zip
cd ~/projects/HugzzBotzX
```

Configure Git identity if not already set:

```bash
GH_USER="$(gh api user --jq .login)"
GH_ID="$(gh api user --jq .id)"
git config --local user.name "$GH_USER"
git config --local user.email "${GH_ID}+${GH_USER}@users.noreply.github.com"
```

Commit and push to the repository already configured as `origin`:

```bash
git add -A
git commit -m "Upgrade HugzzBotzX Family V3"
git push origin main
```

Do not create a new repository if the current `origin` is already the intended repository. Check with `git remote -v` first.

GitHub Actions builds the debug APK. Open the repository's Actions tab, select the latest successful `Build HugzzBotzX APK` run, and download `HugzzBotzX-Family-v3-debug-apk`.
