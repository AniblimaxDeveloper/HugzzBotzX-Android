# V6.1 feature map

| Section | Function |
|---|---|
| HOME | Live device snapshot, quick diagnostic and report actions |
| CONTROL | Open Android Wi-Fi, Bluetooth, location, display, sound, power, storage, notifications, permissions, date/time and privacy settings |
| DEVICES | Wireless ADB authorization guide and copyable read-only diagnostic commands |
| APPS | Launcher-visible application inventory, launch and App info shortcuts |
| TOOLS | Flashlight, slow SOS, Usage Access shortcut and focus timer |
| BUG LAB | Bounded local CPU, memory and exception tests |
| FAMILY | Family-page PIN gate with salted PBKDF2 hash and custom routine list |
| LOGS | Local activity history, clear and report export |
| MORE | Usage report, app permissions, Android settings and full report |

## Honest capability notes

The app does not execute shell commands against another device. It copies diagnostic ADB commands for Termux; users must pair using Android Wireless Debugging and approve the target-side prompt. The app's built-in controls affect the current phone or open Android Settings. Cross-device control is not implemented in V6.1.
