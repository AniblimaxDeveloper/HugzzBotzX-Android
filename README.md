# HugzzBotzX V8.0 — Ultimate Comic Command

A colorful Android Native Java control, media, security and family utility panel.

## Included
- Home dashboard, System Center, Android Control Center and Permission Center.
- Authorized ADB pairing helper, local device registry, support session records and read-only diagnostic command snippets.
- Launcher-visible App Manager, local performance Bug Lab, flashlight and slow SOS.
- Consent-first Sender (local opt-in records + one-recipient manual drafts, no bulk auto-send).
- Media Center with images/video/audio categories, partial photo/video access, Android picker, APK metadata inspection and SHA-256 file hashing.
- Security Center, local admin roles, Android Keystore AES-GCM token vault and bot FAQ templates.
- Live local monitoring, local reminders, activity logs, JSON backup/restore and app-private Private Vault notes.
- Original HugzzBotzX launcher logo retained from the user-provided artwork.

## Important
This is not a stealth RAT. No silent remote control, hidden media access, keylogging, malware, exploit, destructive device payload, or unsolicited bulk messaging is included. ADB requires target-side Wireless Debugging approval. Support session entries are local consent/audit records, not live screen sharing. Google OAuth, cloud sync, official messaging APIs and remote assistance require a separately deployed backend and provider credentials.

## Permissions
The app does not request all permissions on first launch. It asks at the point of use. Images, video and audio use granular runtime permissions and/or the Android Photo Picker / Storage Access Framework. No `MANAGE_EXTERNAL_STORAGE` all-files permission is declared. Media is not scanned in the background.

## Build
- JDK 17
- AGP 9.5.0
- Gradle 9.6.0
- Android SDK 36 / Build Tools 36.0.0
- `gradle --no-daemon --stacktrace :app:assembleDebug`

See `docs/V8-FEATURES.md`, `docs/V8-TEST-MATRIX.md`, and `docs/V8-TERMUX.md`.
