# Media access and runtime permission behavior

HugzzBotzX does not obtain unrestricted shared-storage access at startup. Open MEDIA and tap GRANT MEDIA LIBRARY ACCESS to see Android's runtime permission prompt. The runtime grant remains until you revoke it in Android Settings; this build only queries the media library from the Media Center and does not run background media scans.

For individual files, use PICK AN IMAGE / VIDEO / AUDIO. Android's system document picker grants access to the chosen URI without giving the app access to the rest of the library.

On Android 14 and later, the user can choose selected photos/videos instead of full image/video access. If partial access is granted, the Media Center shows only items made visible by Android.

The app does not request MANAGE_EXTERNAL_STORAGE. That special permission is not appropriate for a diagnostic/communications app and is not needed for the included picker, hash and APK-inspection workflows.
