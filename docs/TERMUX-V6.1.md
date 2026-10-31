# Apply V6.1 to existing Termux checkout

The full archive contains a folder named `HugzzBotzX-V6.1`. Extract it beside your existing checkout, then copy project contents while preserving the existing `.git` folder:

```sh
cd ~/projects
unzip -o /sdcard/Download/HugzzBotzX-V6.1.zip -d ~/projects
cp -a ~/projects/HugzzBotzX-V6.1/. ~/projects/HugzzBotzX/
cd ~/projects/HugzzBotzX
git status --short
git add -A
git commit -m "Upgrade HugzzBotzX V6.1 Comic Command Control"
git push origin main
```

Do not run `git init` again. Do not delete, replace or copy over `.git`.

For the smaller patch archive, extract it directly into the existing checkout:

```sh
cd ~/projects/HugzzBotzX
unzip -o /sdcard/Download/HugzzBotzX-V6.1-Patch.zip -d .
```
