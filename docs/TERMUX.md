# Termux → GitHub → APK

## A. Install the tools

```bash
termux-setup-storage
pkg update -y
pkg install git gh unzip -y
```

Authenticate GitHub once:

```bash
gh auth login
```

Choose GitHub.com, HTTPS, and browser/device-code authentication.

## B. Replace the old source safely

Download `HugzzBotzX-Premium-V2.zip` into Android's Download folder. Back up the existing project first:

```bash
cd ~/projects
cp -a HugzzBotzX "HugzzBotzX-backup-$(date +%Y%m%d-%H%M%S)"
```

Extract the new source over the project folder (the existing `.git` directory is not inside the ZIP and remains untouched):

```bash
unzip -o /sdcard/Download/HugzzBotzX-Premium-V2.zip -d ~/projects
cd ~/projects/HugzzBotzX
```

The ZIP contains the project source, icon assets, README, and GitHub Actions workflow. It intentionally does not include `.git` or build outputs.

## C. Set Git identity correctly

The setting names must be `user.name` and `user.email` (not your username/email as the key):

```bash
cd ~/projects/HugzzBotzX
GH_USER="$(gh api user --jq .login)"
GH_ID="$(gh api user --jq .id)"
git config --local user.name "$GH_USER"
git config --local user.email "${GH_ID}+${GH_USER}@users.noreply.github.com"
```

Verify:

```bash
git config --get user.name
git config --get user.email
```

## D. Commit and push

```bash
git branch -M main
git add .
git commit -m "HugzzBotzX Premium V2"
```

Create the empty repository if it does not exist, then connect it:

```bash
GH_USER="$(gh api user --jq .login)"
if ! gh repo view "$GH_USER/HugzzBotzX-Android" >/dev/null 2>&1; then
  gh repo create HugzzBotzX-Android --public
fi

git remote get-url origin >/dev/null 2>&1 \
  && git remote set-url origin "https://github.com/$GH_USER/HugzzBotzX-Android.git" \
  || git remote add origin "https://github.com/$GH_USER/HugzzBotzX-Android.git"

git push -u origin main
```

If `git commit` says there is nothing to commit, that means the current source has already been committed; proceed to the remote/push commands.

## E. Get the APK

Open the repository → **Actions** → **Build HugzzBotzX Premium APK**. The workflow produces a debug-signed APK as an artifact. Download and extract `HugzzBotzX-Premium-debug-apk`, then install `app-debug.apk`.

If the workflow fails, open the failed run and copy the first red error section. Do not delete `.git`, reinitialize the repository, or make a second repository just to fix a Gradle error.
