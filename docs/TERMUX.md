# Build HugzzBotzX Family V4 from Termux and GitHub

Download the ZIP, then extract it to a new folder or overwrite your existing working copy carefully. Keep the existing `.git` directory if you are updating the same Git repository.

```bash
termux-setup-storage
cd ~/projects
unzip -o /sdcard/Download/HugzzBotzX-Family-V4.0.zip -d ~/projects
cd ~/projects/HugzzBotzX

GH_USER="$(gh api user --jq .login)"
GH_ID="$(gh api user --jq .id)"
git config --local user.name "$GH_USER"
git config --local user.email "${GH_ID}+${GH_USER}@users.noreply.github.com"

git add -A
git commit -m "Upgrade HugzzBotzX Family V4"
git push origin main
```

The GitHub Actions workflow uses JDK 17, Android SDK Platform 36, Build Tools 36.0.0 and Gradle 9.6.0. Open the repository Actions tab and download artifact `HugzzBotzX-Family-v4-debug-apk` after the job succeeds.
