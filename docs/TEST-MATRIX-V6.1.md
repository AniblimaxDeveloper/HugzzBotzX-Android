# V6.1 manual test matrix

Run these checks on an Android 8.0+ device (some OEM settings pages can be unavailable):

1. Launch the app; confirm comic UI and the supplied HugzzBotzX icon.
2. Home: verify Android version/model, memory, storage, battery and network labels.
3. Control: open Wi-Fi, Internet, Bluetooth, Location, Display, Volume, Battery, Storage and Privacy settings; return to the app after each.
4. Apps: verify launcher-visible apps appear; tap Open; long-press an entry to open Android App info.
5. Devices: open Wireless Debugging settings; copy each diagnostic command; verify no command executes automatically.
6. Tools: grant Camera permission when prompted; toggle torch; start SOS and confirm automatic stop within 10 seconds; stop manually.
7. Focus: start 15-minute timer, navigate to another tab and back; confirm countdown continues while process is alive.
8. Usage: open Usage Access settings, enable access, return and open Usage Analytics. Empty data is valid on a device with no recorded usage.
9. Bug Lab: run 2-second CPU test, 8 MB memory test and caught exception demonstration.
10. Family: save a 4–8 digit PIN and a custom routine; leave and re-enter Family to verify the PIN gate; verify routine persistence after relaunch.
11. Logs: confirm actions appear; export a report; clear local log.

Build validation is performed in GitHub Actions with JDK 17, Android SDK 36, AGP 9.5.0 and Gradle 9.6.0. The local container used to prepare this source does not include Android SDK, so no local APK build is claimed.
