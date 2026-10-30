# HugzzBotzX Family V3.0

A native Android family wellbeing dashboard with a premium light theme. Built in Java using Android SDK APIs.

## Current app capabilities

- On-device battery, storage, RAM and network snapshot.
- Optional Usage Access and today's app foreground-use report.
- Daily screen-time soft goal (1–5 hours); it does not force-close or block other apps.
- In-app 25/45-minute focus timer.
- Device flashlight on/off after runtime Camera permission.
- Slow, time-limited SOS flash pattern (about 1 blink/second, 10 seconds maximum).
- Local activity log and report export through Android Share.
- User-supplied HugzzBotzX icon in launcher resources and app UI.

## Important scope

V3 is an on-device app only. No remote device commands, remote monitoring, background surveillance, screen capture, camera/microphone capture, or app blocking on a second phone is implemented. The family connection card is intentionally an explanation, not a fake pairing flow. A future remote version needs a secure backend, authentication and explicit approval on the supervised device. Stronger Android policy enforcement requires supported supervised-device or enterprise enrollment.

## Build requirements

- JDK 17
- Android SDK Platform 36 and Build Tools 36.0.0
- Gradle 9.6.0
- Android Gradle Plugin 9.5.0

## GitHub Actions

Push to `main` or start the `Build HugzzBotzX APK` workflow manually. Download the `HugzzBotzX-Family-v3-debug-apk` artifact after the workflow succeeds.

## Permissions

- Camera: requested only when the user activates the flashlight/SOS.
- Usage Access: enabled manually by the user in Android Settings; optional.
- Network state: read-only connectivity status.

No Internet permission is declared in this version.
