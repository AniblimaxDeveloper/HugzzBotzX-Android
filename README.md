# HugzzBotzX Premium — Android V2.0

HugzzBotzX is a premium dark-themed Android dashboard for transparent, owner-consented device support. The app is built in native Java and uses the artwork supplied by the project owner as its launcher and in-app logo.

## V2.0 features that work locally

### Dashboard
- Premium midnight UI with four pages and persistent bottom navigation.
- Device manufacturer/model, Android version/API level, screen resolution and uptime.
- Live battery percentage, charge state and temperature when Android exposes them.
- Available/internal storage and available/total RAM.
- Current network type/status (read-only; requires `ACCESS_NETWORK_STATE`).
- One-tap refresh of device diagnostics.

### Tools
- Six-digit cryptographically random demo invitation code.
- Five-minute expiry countdown.
- Copy code to Android clipboard.
- Share invitation using the Android Sharesheet.
- Export a text diagnostic report using the Android Sharesheet.
- Privacy/capability checklist.

### Activity
- Records local app events such as invitation creation, copy/share, diagnostics refresh, and session start/end.
- Stores the latest 60 entries in private app preferences.
- Clear history with a confirmation dialog.

### Consent session
- Explicit consent dialog before starting.
- Persistent on-screen session state while the app is open.
- Elapsed session timer and visible stop control.
- Start/end events are saved to local activity history.

### Settings
- App version/package/build information.
- Privacy notes and owner-control checklist.
- Export report, clear activity history, reset demo invitation, and About dialog.

## Important scope

This is a **local-only support dashboard**, not a remote-control or surveillance application. The six-digit invitation code is a demonstration token only: it is not a network credential and cannot pair or connect another device. A consent session is a visible local timer/state only.

The app has no `INTERNET` permission and does not include screen capture, camera, microphone, Accessibility Service, remote input, notification reading, file/message collection, hidden services, or stealth behavior. A future legitimate remote-support implementation would need a real authenticated backend plus an explicit approval flow and persistent indicators on the assisted device.

## Project structure

```text
HugzzBotzX/
├── app/
│   ├── build.gradle
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/hugzzbotzx/app/MainActivity.java
│       └── res/
│           ├── drawable-nodpi/hugzzbotzx_logo.png
│           ├── mipmap-*/ic_launcher.png
│           └── mipmap-anydpi-v26/ic_launcher.xml
├── .github/workflows/android.yml
├── build.gradle
├── settings.gradle
└── docs/TERMUX.md
```

## Build on GitHub Actions

1. Push the project to your GitHub repository's `main` branch.
2. Open **Actions** and select **Build HugzzBotzX APK**.
3. Open the successful run and download the `HugzzBotzX-v2-debug-apk` artifact.
4. Extract the artifact ZIP and install `app-debug.apk` on an Android device.

The workflow uses JDK 17, Android SDK 36 / Build Tools 36.0.0, Android Gradle Plugin 9.5.0, and Gradle 9.6.0.

## Build locally

With JDK 17, Android SDK platform 36, Build Tools 36.0.0, and Gradle 9.6.0 installed:

```bash
gradle --no-daemon :app:assembleDebug
```

APK output:

```text
app/build/outputs/apk/debug/app-debug.apk
```
