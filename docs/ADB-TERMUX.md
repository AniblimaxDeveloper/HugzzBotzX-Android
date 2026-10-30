# Termux workflow for authorized ADB

Install platform tools:

```bash
pkg update
pkg install android-tools
adb version
```

On the target phone, enable Developer options → Wireless debugging and choose Pair device with pairing code. On the host Termux:

```bash
adb pair <PAIR_IP>:<PAIR_PORT>
```

Enter the one-time code displayed on the target. Then use the *separate* connection IP and port:

```bash
adb connect <DEVICE_IP>:<CONNECT_PORT>
adb devices -l
```

Safe read-only examples:

```bash
adb shell getprop ro.build.version.release
adb shell dumpsys battery
adb shell df -h /data
adb shell dumpsys meminfo
adb shell pm list packages -3
adb logcat -d -t 300
```

Use only with the device owner's permission. Logs may contain sensitive information. To disconnect, use `adb disconnect`; to revoke long-term trust, remove this host from the target's Wireless debugging paired-device list.
