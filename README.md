# HugzzBotzX V7.1 — Live Device Management

Android Native Java app with colorful comic UI and local-first admin, sender, media, security and device tooling.

## V7.1 additions
- Wireless ADB pairing wizard: validates host/ports, stores device profile, copies `adb pair` command. Pairing code is typed into Termux and never stored by the app.
- Device registry with separate pairing and connection ports, connect/disconnect command helpers, safe read-only diagnostics, and local profile removal.
- Support session manager with current owner approval, purpose, start/end time and local audit history. It is a local session record, not remote control or screen streaming.
- Per-category media access (images, videos, audio), visible permission status and per-action Android picker fallbacks.
- Version 7.1.0 / versionCode 71; workflow artifact `HugzzBotzX-V71-debug-apk`.

## Modules
HOME • CONTROL • DEVICES • APPS • TOOLS • BUG LAB • SENDER • MEDIA • SECURITY • ADMIN • BOTS • MONITOR • NOTIFY • BACKUP • FAMILY • LOGS • MORE

## Build
- JDK 17
- Android Gradle Plugin 9.5.0
- Gradle 9.6.0
- Android SDK 36 / Build Tools 36.0.0

Termux ADB setup guide: `docs/TERMUX-V7.1.md`

GitHub Actions: `.github/workflows/android.yml`

## Termux update
Extract over the existing `~/projects/HugzzBotzX` repository and preserve `.git`, then:

```bash
git add -A
git commit -m "Upgrade HugzzBotzX V7.1 Live Device Management"
git push origin main
```

## ADB safety and limits
- Pairing uses Android Wireless debugging and requires the target owner to display and approve the pairing code.
- The app copies ADB commands; it does not run arbitrary shell commands, silently connect, or bypass Android authorization.
- A stored device profile is not a live connection status. Confirm ADB state with `adb devices -l` in Termux.
- Local support sessions record owner approval and session timing; they do not stream or control a remote screen.
- To revoke ADB host trust, the target owner must remove the host from Developer options → Wireless debugging → Paired devices. Removing a local profile is not target-side revocation.

## Media privacy
- Permissions are requested on the Media screen, category by category, or when the user selects the explicit all-categories option.
- Android Photo Picker / Storage Access Framework can grant access to only a user-selected item.
- No `MANAGE_EXTERNAL_STORAGE` permission is requested. No media is scanned in the background.
- API integrations, cloud sync, Google OAuth, official bot delivery and live remote support require a separately deployed secure backend and provider credentials.
