# HugzzBotzX V7.0 — Ultimate Comic System

Android Native Java app with colorful comic-style UI, local device diagnostics, user-authorized ADB helpers, consent-first messaging drafts, media center, security checks, local admin workspace, bot FAQ templates, monitoring, notifications and backup/restore.

## Main modules
HOME • CONTROL • DEVICES • APPS • TOOLS • BUG LAB • SENDER • MEDIA • SECURITY • ADMIN • BOTS • MONITOR • NOTIFY • BACKUP • FAMILY • LOGS • MORE

## Build
- JDK 17
- Android Gradle Plugin 9.5.0
- Gradle 9.6.0
- Android SDK 36 / Build Tools 36.0.0

GitHub Actions workflow: `.github/workflows/android.yml`
Artifact name: `HugzzBotzX-V7-debug-apk`

## Termux update
Extract this project over the existing `~/projects/HugzzBotzX` repository (preserve `.git`) or extract into a new folder, then run:

```bash
git add -A
git commit -m "Upgrade HugzzBotzX V7 Ultimate System"
git push origin main
```

## Media access
Media permissions are not requested at launch. The user can select one item through Android's system picker or explicitly grant image/video/audio library access from the MEDIA page. Android 14 selected-photo access is supported. No all-files `MANAGE_EXTERNAL_STORAGE` permission is used.

## Important limitations
External provider APIs, Google OAuth, live remote screen control, cloud multi-device sync, cloud database and webhook delivery require a separately deployed secure backend and provider credentials. The V7 build includes local workflows/configuration only and does not claim these external features are live. Sender opens reviewed drafts for one opted-in recipient at a time; it does not bulk-send or send silently. Security checks are metadata/permission analysis, not a certified antivirus.
