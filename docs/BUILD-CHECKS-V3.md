# V3 source validation

Validated in the authoring environment:

- All Android XML resource and manifest files parse as well-formed XML.
- Adaptive launcher icon XML points to an existing `icon_bg` color and launcher foreground mipmap.
- Launcher PNGs exist for mdpi, hdpi, xhdpi, xxhdpi and xxxhdpi; original user artwork is also embedded in the app header.
- `MainActivity.java` braces and delimiters are balanced; source parser check found no Java syntax diagnostics.
- App manifest declares only Camera (for user-triggered torch), Usage Stats (special user-controlled access), and Network State permissions. It declares no Internet, accessibility, overlay, microphone, contacts, location or screen-capture permission.

Not validated in this environment: Android Gradle compilation and APK runtime execution, because an Android SDK `android.jar` and Gradle installation are not available here. GitHub Actions performs the actual `:app:assembleDebug` build after the source is pushed.
