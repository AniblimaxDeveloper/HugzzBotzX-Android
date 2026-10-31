# HugzzBotzX V7.1 — Termux ADB helper

On the device running Termux (the ADB host):

```bash
pkg update
pkg install android-tools
adb version
```

On the target device, the owner must enable Developer options → Wireless debugging and open **Pair device with pairing code**.

In HugzzBotzX, open DEVICES → PAIR A DEVICE, enter the target's IP/hostname and pairing port, confirm owner approval, and copy the pair command. Run it in Termux:

```bash
adb pair HOST:PAIRING_PORT
```

Enter the temporary pairing code only when Termux asks for it. Do not paste the code into HugzzBotzX or save it in notes.

After pairing, return to the main Wireless debugging page on the target and read its separate connection IP/port. Save that port in the HugzzBotzX registry, then run the copied connection command in Termux:

```bash
adb connect HOST:CONNECT_PORT
adb devices -l
```

A device listed as `device` is connected for ADB. `unauthorized` means its owner still needs to accept the RSA trust prompt. `offline` means the connection is not ready. Use only read-only diagnostic snippets supplied by HugzzBotzX unless you fully understand a command and have explicit authorization.

To disconnect:

```bash
adb disconnect HOST:CONNECT_PORT
```

To revoke the host's trusted ADB key, the target owner must remove this workstation from Developer options → Wireless debugging → Paired devices. Deleting a registry profile in HugzzBotzX only deletes the local note.
