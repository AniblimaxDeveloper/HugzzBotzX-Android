# V8 validation matrix

| Area | Static check | Device test required |
|---|---|---|
| Java | source parsed / methods and braces checked | Full Gradle compile + launch |
| XML | all XML files parsed | Install on API 26, 33, 34, 35, 36 |
| Media permissions | granular manifest permissions present | Test full, selected-only and denied states |
| Photo/document picker | ACTION_PICK_IMAGES / ACTION_OPEN_DOCUMENT code present | Test images, videos, audio, APK and cancellation |
| Flashlight | Camera2 path and permission checks present | Device with and without rear flash |
| Notifications | receiver manifest and permission flow present | Android 13+ grant/deny and reminder delivery |
| ADB | validated host/ports and copied commands | Pair/approve/connect/disconnect in Termux |
| Sender | opt-in check, unsubscribe and manual draft | Test SMS/email/share handlers; no auto-send |
| Security | APK/permission metadata and SHA-256 path | Test valid/invalid APK and large-file handling |
| Private Vault | path sanitization and 64 KB cap | Create/read/edit/delete notes; verify no shared storage |
| Backup | JSON export/import with confirmation | Backup/restore and malformed-file rejection |
| Keystore | AES-GCM vault class | Test save/read after restart and device credential changes |
| Edge-to-edge | API 35+ insets path | Test status/navigation bar on Android 15/16 |

The source package was statically validated in this environment. A full Android SDK compile and physical-device test must be completed by GitHub Actions / a developer device before claiming release readiness.
