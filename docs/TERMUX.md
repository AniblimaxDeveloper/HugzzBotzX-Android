# Termux workflow

This project is designed so Termux can create/push the source while GitHub Actions performs the Android SDK build.

## 1. Install the command-line tools

```bash
pkg update -y
pkg install -y git gh unzip
```

If Termux cannot reach its configured repository, fix/change the Termux mirror first with `termux-change-repo`, then run the install commands again.

## 2. Put the project in your Termux home

Extract the downloaded project ZIP, then enter its folder. For example, if the ZIP is in Downloads:

```bash
termux-setup-storage
mkdir -p ~/projects
unzip /sdcard/Download/HugzzBotzX.zip -d ~/projects/
cd ~/projects/HugzzBotzX
```

## 3. Push to GitHub

```bash
git init
git add .
git commit -m "Initial HugzzBotzX consent support app"
gh auth login
```

Create the remote repository and push:

```bash
gh repo create HugzzBotzX-Android --public --source=. --remote=origin --push
```

If the repository already exists, use its URL instead:

```bash
git remote add origin https://github.com/USERNAME/HugzzBotzX-Android.git
git branch -M main
git push -u origin main
```

Replace `USERNAME` with your own GitHub username. Do not upload GitHub tokens, passwords, or private keys into this project.

## 4. Download the APK

On GitHub, open `HugzzBotzX` → **Actions** → latest successful **Build HugzzBotzX APK** run → **Artifacts** → `HugzzBotzX-debug-apk`.
