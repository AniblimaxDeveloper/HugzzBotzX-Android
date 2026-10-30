# HugzzBotzX DevLab V5.0

Native Android Java app based on Family V4, upgraded with an **authorized ADB assistant** and a **bounded local Bug Lab**. The pearl-white / lavender visual theme and user-supplied HugzzBotzX launcher logo are retained.

## V5 additions

### Authorized ADB assistant
- Explains Android Wireless debugging pairing for a device the user owns or has permission to inspect.
- Opens Developer Options on the current phone.
- Copies Termux `android-tools` installation, `adb pair`, `adb connect`, and `adb devices -l` command templates.
- Provides copy buttons for read-only commands: Android version, battery status, storage, memory, third-party package inventory and a short logcat tail.
- ADB is executed manually in Termux; HugzzBotzX does not embed an ADB daemon, connect automatically, bypass pairing or store pairing codes.
- The target phone must enable Wireless debugging and the owner must approve the pairing. Target and host must be on the same trusted Wi-Fi network. Android 11+ is required for the built-in Wireless debugging workflow.

### Local Bug Lab
- Single-worker CPU micro-benchmark capped at 3 seconds.
- Temporary 4 MB memory allocation and page-touch check; buffer is immediately released.
- Caught exception simulation to verify error-reporting behavior without crashing the app.
- Network-state check using Android connectivity APIs.
- Local diagnostic report export through the Android share sheet.
- All tests run only inside the app on the phone where it is installed. They do not send workloads or payloads to other devices.

## Existing V4 family tools retained
- Device dashboard, battery / RAM / storage / connectivity overview.
- Local family profile, optional parent PIN, screen-time insights using Usage Access.
- Daily routine checklist, planner, focus timer and local check-in.
- Camera2 flashlight and slow bounded SOS pattern with runtime permission.
- Local activity history, report export and privacy shortcuts.

## Safety and limitations
- No crash/freeze payloads, exploits, vulnerability scanning against third-party devices, network flooding, bootloop commands, destructive ADB commands, stealth control, keylogging, hidden screen capture, microphone, or hidden service.
- ADB command templates are read-only. The user manually runs them after target-side pairing approval.
- Logcat can contain private data. Review and redact logs before sharing.
- To revoke ADB trust, remove the host in the target phone's Wireless debugging paired-device list or revoke debugging authorizations.
- The app does not implement cloud accounts or remote-control backend in this version.

## Build requirements
- JDK 17
- Android SDK Platform 36 and Build Tools 36.0.0
- Gradle 9.6.0
- Android Gradle Plugin 9.5.0

## Build

```bash
gradle --no-daemon :app:assembleDebug
```

APK output: `app/build/outputs/apk/debug/app-debug.apk`.

## GitHub Actions

Push to `main` or manually run `Build HugzzBotzX APK`. Artifact: `HugzzBotzX-DevLab-v5-debug-apk`.
