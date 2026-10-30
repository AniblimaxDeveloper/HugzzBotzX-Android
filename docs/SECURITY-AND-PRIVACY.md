# Security and privacy — V5.0

- This app does not declare Internet permission and has no server connection.
- Camera permission remains runtime-requested only for local flashlight / SOS.
- Usage Access is optional and granted manually in Android Settings.
- The ADB assistant is a local command-copy helper. Pairing credentials are not stored in app preferences.
- ADB must be run by the user in Termux. Wireless debugging is enabled on the target phone by its owner, who approves pairing.
- Read-only command templates are provided. There are no destructive shell commands, payload delivery, exploits or silent access paths.
- The Bug Lab runs bounded tests on the current app process only: one CPU worker for at most 3 seconds, a temporary 4 MB buffer, and a caught exception simulation.
- Logcat may contain private application or device information; review and redact before sharing.
- Parent PIN, family profile and activity history remain in local private app preferences.
- The target owner can revoke ADB pairing from Wireless debugging → Paired devices → Forget or revoke debugging authorizations.
