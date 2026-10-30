# Feature map — HugzzBotzX Premium V2.0

| Screen | Feature | Behavior |
|---|---|---|
| Dashboard | Device identity | Reads manufacturer, model, Android version and API level from Android system APIs |
| Dashboard | Battery card | Reads level and charging state from the sticky battery broadcast |
| Dashboard | Storage card | Reads app-visible data partition capacity through `StatFs` |
| Dashboard | RAM card | Reads available and total RAM through `ActivityManager.MemoryInfo` |
| Dashboard | Network status | Reads active network type/status; no network payload is sent |
| Dashboard | Display and uptime | Reads display metrics and elapsed boot time |
| Tools | Demo invitation | Generates a random six-digit local code and expires it after five minutes |
| Tools | Copy invitation | Copies the current unexpired code to Android clipboard |
| Tools | Share invitation | Opens Android Sharesheet with a clear demo-only disclaimer |
| Tools | Diagnostics refresh | Re-reads diagnostic data and records a local event |
| Tools | Report export | Builds a text report and opens Android Sharesheet |
| Activity | Event history | Persists up to 60 local events in private SharedPreferences |
| Activity | Clear history | Requires confirmation, then deletes the local event list |
| Session | Consent dialog | Requires the user to accept before the visible demo session starts |
| Session | Timer and stop button | Shows elapsed local duration and ends immediately when requested |
| Settings | Reset demo invitation | Removes the current local demo code |
| Settings | About | Shows version and app scope |

## Explicitly not implemented

No real remote pairing, remote input, screen viewing, camera/microphone capture, Accessibility Service, notification reading, file/message access, stealth background process, or hidden control. The demo pairing string is not a server credential and cannot contact another device.
