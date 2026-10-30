# Feature map — V2.0

| Area | Feature | Implementation |
|---|---|---|
| Home | Live device metrics | Android battery sticky intent, StatFs, ActivityManager, ConnectivityManager |
| Home | Temporary invite | SecureRandom six-digit code, five-minute expiry, clipboard, revoke |
| Device | Device details | Build properties, display metrics, locale/timezone, package version |
| Device | Resource details | Battery level/status/temperature, free/total storage, available/total RAM, network type, uptime |
| Sessions | Explicit consent | AlertDialog, no session starts before acceptance |
| Sessions | Visible timer | Monotonic elapsed-realtime clock |
| Sessions | Auto-stop | Session ends when Activity leaves foreground |
| Sessions | Audit trail | App-private SharedPreferences, newest-first, max 30 entries |
| Tools | Diagnostics | Local battery/storage/memory/network/permission checks |
| Tools | Reports | Clipboard and Android share sheet |
| Tools | System shortcuts | Android Settings and app-details screens |
| Tools | Help/privacy/about | In-app explanatory dialogs |

## Deliberate exclusions

No screen streaming, remote touch/keyboard injection, camera/microphone streaming, message/file access, Accessibility Service, hidden services, device-admin takeover, or invisible persistence. A real remote-support product would need a separate consented client/server design, authenticated pairing, session indicators, revocation, and auditable data handling; the current demo code must not be represented as real pairing.
