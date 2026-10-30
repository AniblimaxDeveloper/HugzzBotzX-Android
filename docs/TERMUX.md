# Termux + GitHub Actions workflow

The Android source is prepared in Termux. GitHub Actions installs the Android SDK and builds the APK, so you do not need to install a full Android SDK on your phone.

## 1. Download and extract V2

Save `HugzzBotzX-Premium-V2.0.zip` into your Android Downloads folder, then run:

```bash
termux-setup-storage
pkg update -y
pkg install -y git gh unzip
mkdir -p ~/projects
unzip -o /sdcard/Download/HugzzBotzX-Premium-V2.0.zip -d ~/projects
cd ~/projects/HugzzBotzX
```

When updating an existing checkout, extracting to `~/projects` overwrites tracked project files but leaves the hidden `.git` directory intact.

## 2. Configure Git identity correctly

```bash
GH_USER="$(gh api user --jq .login)"
GH_ID="$(gh api user --jq .id)"
git config --local user.name "$GH_USER"
git config --local user.email "${GH_ID}+${GH_USER}@users.noreply.github.com"
git branch -M main
```

The configuration keys are `user.name` and `user.email` — do not use the account name or email address as the key.

## 3. Commit the V2 project

```bash
git add -A
git commit -m "Upgrade HugzzBotzX Premium V2"
```

If Git says there is nothing to commit, check `git status` and make sure the ZIP was extracted into `~/projects`, not into a nested `HugzzBotzX/HugzzBotzX` folder.

## 4. Create or connect the GitHub repository and push

```bash
GH_USER="$(gh api user --jq .login)"
if gh repo view "$GH_USER/HugzzBotzX-Android" >/dev/null 2>&1; then
  echo "Repository already exists"
else
  gh repo create HugzzBotzX-Android --public || exit 1
fi

if git remote get-url origin >/dev/null 2>&1; then
  git remote set-url origin "https://github.com/$GH_USER/HugzzBotzX-Android.git"
else
  git remote add origin "https://github.com/$GH_USER/HugzzBotzX-Android.git"
fi

git push -u origin main
```

If GitHub rejects the push because the remote already contains a separate initial commit, inspect it first with `git fetch origin` and `git log --oneline --graph --all -10`; do not force-push blindly.

## 5. Download the APK

Open your repository → **Actions** → latest successful **Build HugzzBotzX APK** run → **Artifacts** → `HugzzBotzX-v2-debug-apk`. Extract the artifact ZIP and install `app-debug.apk`.

## Notes

- The APK is a debug build and is suitable for testing. A production release should be signed with a private release key stored as GitHub Actions secrets.
- Do not paste GitHub tokens, passwords, or signing keys into chat or commit them to the repository.
- The current pairing code is demo-only and does not establish a real remote connection.
