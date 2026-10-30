# Security and privacy notes — V4

- The optional parent PIN protects only the Family screen within this app; it is not a secure online identity and cannot replace Android account/device authentication.
- The PIN is salted and hashed using SHA-256, then stored in app-private SharedPreferences.
- Family nickname, emergency phone, check-in, schedule, routine states, per-app soft goals and activity history are local-only.
- Reports are shared only after an explicit user tap through Android's share sheet.
- Usage Access is optional; data is read from Android UsageStatsManager on this device.
- Camera permission is requested only when the flashlight or SOS is invoked.
- No Internet permission is declared in the manifest.
- No remote command, hidden monitoring, camera/microphone capture, screen capture, keyboard capture, or stealth service is present.
