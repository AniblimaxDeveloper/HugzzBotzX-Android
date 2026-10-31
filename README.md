# HugzzBotzX DevLab V6.1 — Comic Command Control

A full-color comic-style Android toolkit for local device controls and authorized diagnostics. Uses the supplied HugzzBotzX logo as the app icon.

## V6.1 features

- Comic UI with nine navigation sections: Home, Control Center, Devices, Apps, Tools, Bug Lab, Family, Logs, More.
- Live local device snapshot: Android version, device model, memory, storage, battery level/temperature/charging state, network transport and CPU core count.
- Control Center shortcuts to Android's Wi-Fi panel, Bluetooth, location, display, sound, battery saver, storage, app notifications, app permissions, date/time and privacy settings.
- App Manager lists launcher-visible apps; tap Open to launch; long-press a row to open that app's Android App info page.
- Usage Analytics for the previous 24 hours after the user explicitly grants Usage Access.
- ADB helper copies read-only diagnostic commands for Termux. The app does not execute ADB or pair silently; the target device must enable Wireless Debugging and approve the RSA/pairing prompt.
- Flashlight toggle and slow SOS pattern with a 10-second automatic stop.
- Focus timer (15/25/45 minutes), local family routines, local parent PIN lock (salted PBKDF2 hash), custom routines, activity log and shareable report.
- Bounded Bug Lab: 2-second CPU micro-benchmark, 8 MB allocation test and caught-exception demonstration, all on this device only.

## Control model and limitations

System-control buttons open Android's own Settings panels. Android decides which settings are available and requires the device owner to make protected changes. The ADB snippets are copied to the clipboard and must be reviewed and run manually in Termux after authorization. This APK does not provide hidden remote access, unattended screen control, remote crashing, flooding, bootloops, exploit payloads or destructive commands. A real multi-device remote-support service would require a separate authenticated backend and an explicit visible approval flow on the target device.

## Permissions

- Camera: requested only when flashlight/SOS is used; required by Android camera/torch APIs.
- Usage Access: special Android setting opened by the user to display app usage analytics.
- Network state: read-only connectivity summary.

No broad package visibility permission is requested; App Manager uses launcher-visible activities only. No internet permission is declared in this starter.

## Build

- JDK 17
- Gradle 9.6.0
- Android Gradle Plugin 9.5.0
- Android SDK Platform 36 / Build Tools 36.0.0

Run `gradle --no-daemon :app:assembleDebug` from the repository root. GitHub Actions builds and uploads `HugzzBotzX-V61-debug-apk`.
