# HugzzBotzX

A dark-themed Android starter for **transparent, consent-based device support**.

## What this starter does

- Shows basic information about the device running the app (model, Android version, battery level).
- Generates a six-digit **local demo code**. It is not a network pairing credential and cannot connect to another phone.
- Provides a visible consent dialog and a session indicator that the device owner can stop.
- Uses the supplied HugzzBotzX artwork as the launcher icon and in-app branding asset.

## Safety and scope

This first build is intentionally a local demo, not a remote-control or surveillance tool. It has no Internet permission and requests no sensitive permissions. It does not capture the screen, camera, microphone, keystrokes, notifications, files, or messages; it does not use an Accessibility Service; and it does not run a hidden background service.

A future legitimate support version should use authenticated pairing, clear consent on the controlled device, a persistent session indicator, revocable access, and platform permission prompts for each sensitive capability.

## Build on GitHub Actions

1. Push this project to a GitHub repository named `HugzzBotzX`.
2. Open the repository's **Actions** tab.
3. Run **Build HugzzBotzX APK** or push a commit to `main`.
4. Open the successful workflow run and download the `HugzzBotzX-debug-apk` artifact.

The workflow uses JDK 17, Android SDK 36, Android Gradle Plugin 9.5.0, and Gradle 9.6.0.

## Build locally

Use JDK 17, Android SDK platform 36, Build Tools 36.0.0, and Gradle 9.6.0:

```bash
gradle --no-daemon :app:assembleDebug
```

The debug APK is generated at `app/build/outputs/apk/debug/app-debug.apk`.
