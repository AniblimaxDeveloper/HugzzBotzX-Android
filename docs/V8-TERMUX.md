# Update existing Termux repository to V8

1. Download `HugzzBotzX-V8.0-Full-Source.zip` to Android Downloads.
2. Extract and overlay the project while preserving `.git`:

```sh
cd ~/projects
unzip -o /sdcard/Download/HugzzBotzX-V8.0-Full-Source.zip -d ~/projects
cp -a ~/projects/HugzzBotzX-V8.0/. ~/projects/HugzzBotzX/
cd ~/projects/HugzzBotzX
```

3. Verify version and commit:

```sh
grep -n "versionName\|versionCode" app/build.gradle
git status --short
git add -A
git commit -m "Upgrade HugzzBotzX V8 Ultimate System"
git push origin main
```

Do not delete `.git`. GitHub Actions builds the APK artifact `HugzzBotzX-V8-debug-apk` after push.
