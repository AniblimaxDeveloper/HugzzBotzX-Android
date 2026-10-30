# HugzzBotzX Premium — Android V2.0

Premium monochrome device-support dashboard for Android. Built with native Java and Android SDK; no third-party runtime libraries are required.

## What's included

- Premium obsidian/silver interface with four navigation tabs: Home, Device, Sessions, Tools.
- Launcher icon and in-app brand artwork generated from the logo supplied by the project owner.
- Live local device snapshot: manufacturer/model, Android version/API, CPU ABI, display, battery, network transport, available storage, available RAM, and uptime.
- Temporary six-digit local demo invite with a five-minute expiry, copy, regenerate, and revoke actions.
- Explicit owner-consent dialog for a visible local demo session; session timer; manual stop; automatic stop when the app leaves the foreground.
- Local audit history stored in app-private preferences, with clear-history action.
- Diagnostics for battery, storage, memory, network state, and permission footprint.
- Copy/share device and diagnostic reports using Android's clipboard and system share sheet.
- Shortcuts to Android system settings and this app's system app-info page.
- Help, privacy, and about dialogs.

## Important capability boundary

This V2 build is **local-only**. The invite code is a UI/demo token; it does not pair another phone or connect to a backend. Session state is a local consent demonstration; it does not display another device's screen or control it. No stealth monitoring, hidden remote access, camera/microphone capture, screen recording, SMS/contact access, accessibility control, or hidden background service is included.

The app declares only `android.permission.ACCESS_NETWORK_STATE` (normal permission) to label the current network transport. It does not declare `INTERNET` or request dangerous runtime permissions.

## Build with GitHub Actions

1. Push this project to a GitHub repository on branch `main`.
2. Open **Actions**.
3. Select **Build HugzzBotzX Premium APK**.
4. Run the workflow manually with **Run workflow**, or push a commit.
5. Open the successful run and download artifact **HugzzBotzX-Premium-debug-apk**.
6. Extract the artifact and install `app-debug.apk` on Android. The debug APK is automatically debug-signed by Gradle.

The workflow uses JDK 17, Android SDK platform 36, Android Gradle Plugin 9.5.0, and Gradle 9.6.0.

## Build locally (with a compatible Gradle + Android SDK)

```bash
gradle --no-daemon :app:assembleDebug
```

APK output:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Package

- Application ID: `com.hugzzbotzx.app`
- Debug application ID: `com.hugzzbotzx.app.debug`
- Version: `2.0.0` (versionCode 2)
- Minimum Android: API 23 (Android 6.0)
- Compile SDK: 36
- Target SDK: 35
