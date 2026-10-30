# HugzzBotzX V6 build fix

## Root cause found in the V6 source

`MainActivity.java` calls `Typeface.DEFAULT` but the class was not imported. This causes Java compilation to fail with `cannot find symbol: variable Typeface` (or `cannot find symbol: class Typeface`, depending on compiler output).

The source now imports `android.graphics.Typeface`.

## Additional build reliability changes

- The manifest declares the `tools` namespace at the root element.
- The unused `INTERNET` permission was removed. This build does not implement network transport.
- GitHub Actions explicitly installs Android SDK platform 36 and Build Tools 36.0.0.
- The build step now emits a full stack trace and Gradle warnings.
- AGP 9.5.0 + Gradle 9.6.0 + JDK 17 are retained because they match the official AGP 9.5.0 compatibility table.

## Apply

Copy the patch ZIP contents into the repository root, or unzip it into `~/projects/HugzzBotzX` with overwrite enabled. Commit and push the changed files, then run the `Build HugzzBotzX V6` workflow.
