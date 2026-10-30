# HugzzBotzX DevLab V5.0 — feature checklist

## Added in V5

- Authorized ADB assistant for Android Wireless debugging.
- Open Developer Options on the current phone.
- Copy Termux android-tools install and ADB pair/connect/list commands.
- Copy read-only ADB diagnostics for Android version, battery, storage, memory, package inventory and bounded logcat.
- Safe local Bug Lab with a single CPU worker for 3 seconds maximum.
- Temporary 4 MB memory buffer test with graceful low-memory handling.
- Exception-handling simulation that catches its own test exception and does not crash the app.
- Local network-state inspection.
- Export local device diagnostics through Android share sheet.

## Existing Family V4 features retained

- Live dashboard for battery, storage, RAM and connectivity.
- Family nickname, emergency contact dialer shortcut and parent PIN.
- Usage Access screen-time reports and soft usage goals.
- Daily family routine, custom tasks, check-in and bedtime/study planner.
- Focus timer, Camera2 flashlight and slow max-10-second SOS.
- Local activity log, report export and Android settings shortcuts.

## What is intentionally not implemented

- No remote crash/freeze/lag payloads, exploits, bootloops, network flooding, destructive commands, or bypass of Android consent.
- No automatic hidden ADB connection. The device owner must enable Wireless debugging and approve pairing.
- The app copies commands but does not execute an embedded ADB client; Termux with `android-tools` is the ADB host in this workflow.
- No remote screen viewer, microphone, keylogger, hidden camera, or background remote control.

## ADB workflow summary

1. Put the host phone and target phone on the same Wi-Fi network.
2. On target: enable Developer options → Wireless debugging → Pair device with pairing code.
3. On host Termux: install `android-tools`, run `adb pair IP:PAIR_PORT`, enter the code shown on target.
4. Read the separate connection port from Wireless debugging and run `adb connect IP:CONNECT_PORT`.
5. Verify using `adb devices -l`.
6. Run only read-only diagnostics you understand. Revoke by forgetting the paired host on the target.
