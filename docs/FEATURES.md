# HugzzBotzX Family V4.0 — feature checklist

## Working local features

- Live dashboard for battery, storage, RAM and connectivity.
- Family nickname profile and optional emergency contact shortcut through Android dialer.
- Parent dashboard lock using a locally salted PIN hash.
- Usage Access setup and aggregate today / 7-day app usage reports.
- Per-app soft usage goals (stored locally and shown against measured usage). These do not block or force-close apps.
- Daily screen-time soft goal.
- Daily routine checklist: six built-in tasks plus up to twelve custom tasks, with per-day state.
- Private family check-in with Great / Okay / Need support choices.
- Bedtime and study-start schedule planner. Times are saved as a plan; they do not trigger system alarms or enforce restrictions.
- In-app focus timer.
- Flashlight on/off with Camera2 and slow SOS (about one blink/second, max 10 seconds).
- Local activity log and share-sheet report export.
- Permission Center and shortcut to Android parental controls / Digital Wellbeing settings where supported by the device.

## Multi-device and enforcement limitations

This build does not remotely connect to or control a second device. No server, cloud sync, account backend, remote flash command, remote app blocker, screen viewer or hidden monitoring is included. A production multi-device system requires: authenticated parent accounts, pairing initiated and accepted on the supervised phone, encrypted transport, visible connection status, child-side disconnect/revoke, server authorization rules, and abuse-resistant audit logging.

An ordinary Android app cannot silently take over another phone or generally force app blocks. Device-wide enforcement requires supported parental/supervised or enterprise device-owner enrollment and user-visible provisioning. Use Android Family Link / Digital Wellbeing for enforced controls not available to this ordinary app.

## Flash safety

No rapid turbo strobe is provided. Repetitive rapid flashing can be unsafe for photosensitive people. SOS is slow, short, and automatically stopped. Avoid pointing the torch at eyes or faces.
