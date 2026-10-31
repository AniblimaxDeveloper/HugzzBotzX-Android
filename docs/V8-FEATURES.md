# HugzzBotzX V8.0 — Ultimate Comic Command

## Working local modules
- Colorful comic UI with horizontal navigation and Android 15/16 edge-to-edge handling.
- Device snapshot, system diagnostics, memory, storage, battery, network, security patch, developer-option and device-owner status.
- Android Control Center shortcuts (Wi-Fi/Internet, Bluetooth, location, display, volume, battery, storage, notifications, privacy, security).
- ADB pairing checklist, saved device profiles, copy pair/connect/disconnect commands and read-only diagnostic commands. ADB is executed by the user in Termux after target-side authorization; the app does not bypass pairing.
- Launcher-visible app manager: open apps and their Android App Info screens.
- Flashlight and slow SOS flash, with camera permission and automatic stop.
- Focus timer, family routines and local parent PIN (PBKDF2 salted hash).
- Sender opt-in records, unsubscribe, message templates and single-recipient drafts opened in the user's selected messaging app.
- Media Center: per-category image/video/audio permissions, Android picker, selected URI open/share, MediaStore browsing within granted scope, APK metadata analysis and SHA-256.
- Security Center: visible-app permission declaration audit, selected APK metadata review, file hashing and HTTPS certificate check.
- Admin roles stored locally, AES-GCM token vault backed by Android Keystore, webhook URL configuration, maintenance flag.
- Bot FAQ templates and share-sheet handoff.
- Live local monitor (device metrics and this app's UID traffic counters), local notifications/reminders, activity log and JSON preferences backup/restore.
- V8 Private Vault: small text notes stored in app-private internal storage (64 KB per note).
- Permission Center with explicit runtime request buttons and status cards.
- Integration Center reports whether Google OAuth, cloud sync and provider APIs are actually configured.

## Important integration boundaries
The app does not include a deployed backend, Google OAuth client, Firebase project, WhatsApp Business provider credentials, Telegram bot server or remote screen-sharing service. Token fields are encrypted locally; the app does not claim this means a provider connection is live. Those integrations require separately configured production services and credentials.

No stealth RAT, keylogger, camera/microphone capture, exploit, destructive payload, remote crash/freeze, bulk spam, or silent device control is included. Support sessions and ADB profiles are local records/helpers; target-side authorization remains mandatory.

## Media permission model
The app requests READ_MEDIA_IMAGES, READ_MEDIA_VIDEO, READ_MEDIA_AUDIO and (on Android 14+) READ_MEDIA_VISUAL_USER_SELECTED only after the user enters Media Center and selects a permission action. Android may grant full or selected-only visual media access. Photo Picker / ACTION_OPEN_DOCUMENT remains available for selected items. No MANAGE_EXTERNAL_STORAGE permission is requested and media is not scanned in the background.

## Build configuration
- JDK 17
- Android Gradle Plugin 9.5.0
- Gradle 9.6.0
- compileSdk 36 / targetSdk 36
- Android Build Tools 36.0.0
- applicationId com.hugzzbotzx.app
- versionName 8.0.0 / versionCode 80

Build: `gradle --no-daemon --stacktrace :app:assembleDebug`

## Android 16 note
For target SDK 36, edge-to-edge insets are applied in the activity shell. Android 16 photo/video permission prompts can include app-owned media in the selected-media experience; the app must keep the user's selected-only grant state and not assume broad library access. See Android 16 behavior changes documentation.
