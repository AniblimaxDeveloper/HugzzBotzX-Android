# HugzzBotzX Family V3.0 — Features and limitations

## Working features in this build

- Premium light UI with lavender, pearl-white and soft pink accents.
- User-supplied HugzzBotzX artwork used in the in-app header and Android launcher icon densities.
- Live local device snapshot: battery, free storage, available RAM, device model and network transport.
- Optional Android Usage Access flow and on-device daily app-usage report.
- Configurable daily screen-time soft-goal threshold (1–5 hours). It is a reminder/report threshold, not an app blocker.
- In-app focus timer (25 or 45 minutes) with stop control.
- Flashlight on/off using Android Camera2 torch mode after runtime camera permission.
- Slow SOS flash signal, limited to about one blink per second and automatically stopped after 10 seconds. It is not a rapid strobe.
- Local activity history, local device report and Android share-sheet export.
- Links to Android app permissions and Digital Wellbeing settings.

## Deliberate limitations

This version operates only on the phone on which it is installed. It has no network permission, remote relay, parent account, child-device connection, remote flashlight command, remote app blocking, screen viewing, audio capture, or hidden monitoring. The second-device card explains the prerequisites and does not pretend to be connected.

Android does not allow an ordinary app to silently or generally control another device. A real family service needs an authenticated parent account, a second app installed on the supervised phone, child-side disclosure and acceptance, encrypted transport, a visible active state and a reliable disconnect. Device-wide blocking or policy enforcement also requires supported Android supervision/enterprise enrollment and cannot be granted by an ordinary runtime permission.

The app-usage report requires the user to explicitly enable Usage Access in Android Settings. The flashlight requires the user to grant Camera permission. Both permissions can be revoked in Android Settings.

## Flash safety

No turbo/high-frequency strobe mode is provided. Repetitive flashing can be unsafe for people with photosensitive epilepsy or discomfort. The included SOS is slow, short and automatically stopped. Avoid pointing the light at eyes or faces.
