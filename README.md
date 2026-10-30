# HugzzBotzX Family V4.0

A native Android family wellbeing and parental-guidance app with a premium light blue/lavender UI. Built in Java with Android SDK APIs.

## V4 features implemented on the device

- Refreshed pearl-white / lavender / blue UI and user-supplied HugzzBotzX launcher icon at all densities.
- Live device dashboard: battery, internal storage, RAM, model, Android release, security patch and network type.
- Local family profile nickname and optional trusted emergency contact. The call shortcut opens the system dialer for review; it never calls automatically.
- Optional parent dashboard PIN stored as a salted SHA-256 hash in private app preferences. This is a local access barrier, not an online account.
- Screen-time report for today and the last 7 days using Android Usage Access.
- Daily screen-time soft goal (1–5 h) and per-app soft goals; the app reports and reminds but does not forcibly block or close apps.
- Daily routine checklist with default and up to 12 custom tasks, persisted per day.
- Family wellbeing check-in stored locally.
- Study-start and bedtime planner times stored locally as a planning reference.
- 25/45-minute focus timer.
- Camera2 flashlight and slow, maximum-10-second SOS pattern after runtime Camera permission.
- Local activity history and shareable device/family report.
- Shortcuts to Android Usage Access, Digital Wellbeing, system parental controls (when exposed by the OEM), and app-permission settings.
- Permission Center summarizing Usage Access and Camera permission states.

## Honest multi-device scope

V4 is still an on-device app. It does not claim to connect to, monitor or control another phone. Real remote family supervision requires an authenticated backend, a parent account, a second app installed on the supervised phone, an explicit approval flow on that phone, encrypted communication, visible active-session status, and a reliable disconnect control. This release contains no remote command service or data upload. Strong app blocking and device-wide policies require supported Android Family Link / device-owner / enterprise supervision rather than an ordinary app permission.

## Privacy and permissions

- Camera: requested only when the user activates flashlight/SOS.
- Usage Access: granted manually in Android Settings; optional and revocable.
- Network state: read-only connectivity status.
- No Internet permission is declared.
- No screen capture, microphone, keylogging, hidden camera, stealth service, or remote takeover.
- PIN/profile/routine/log information stays in the app's private local preferences.

## Build requirements

- JDK 17
- Android SDK Platform 36 and Build Tools 36.0.0
- Gradle 9.6.0
- Android Gradle Plugin 9.5.0

## GitHub Actions

Push to `main` or manually run `Build HugzzBotzX APK`. Download `HugzzBotzX-Family-v4-debug-apk` after success.
