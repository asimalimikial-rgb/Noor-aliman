# Manual source update — 2026-11-04

## A12 / v1.14 changes
- Replace the plain adhan alert with an immersive fullscreen screen using a custom mosque illustration and the app palette (deep green, gold, and cream).
- Keep a prominent stop button and add an optional volume-key stop setting while the fullscreen alert is visible.
- Update the Android workflow to build signed v1.14 APKs on pushes to A11 and A12 branches; artifact name corrected.
- Update the app version to `1.14` (`versionCode 16`) and expand the device test checklist.
- Screen-off stop is not implemented in this update. Android's screen-off broadcast cannot distinguish a manual power-button press from the display timing out; no setting is exposed for this behavior.

## Earlier A11 source update — 2026-11-03

## Included
- Re-schedule prayer and dhikr alarms after boot, package replacement, and time/timezone changes.
- Add guarded WebView origin checks, external URL scheme allow-list, JS bridge input validation,
  WebView state restoration, and back navigation.
- Use asynchronous MediaPlayer preparation and audio-focus handling for adhan and dhikr playback.
- Add a full-screen adhan alert activity with normal-notification fallback when Android denies
  full-screen intent access.
- Add optional face-down-to-stop behavior; accelerometer listener is active only while adhan plays
  and the setting is enabled.
- Catch exact-alarm SecurityException at scheduling boundaries.
- Enable R8 for release builds with explicit JavaScript-interface keep rules.
- Disable Android app backup and add a privacy policy.
- Clarify owner-confirmed provenance of the supplied salawat recording; both existing copies retained.
- Remove four one-byte `Save`/`save` placeholder files.

## Not changed intentionally
- `compileSdk` and `targetSdk` are 36; Android Gradle Plugin 8.13.2 and Gradle 8.13 are required.
  The build environment must install Android Platform 36.
- No new Haramayn audio was added because no redistribution license was verified.
- No keystore, passwords, signing secrets, or APK release artifacts were included.
- Existing `salawat.mp3` files were not replaced or deleted.

## Validation status
- Source-level checks and JavaScript syntax checks are to be run against this package.
- A real Android Gradle build and device tests are not yet confirmed for A12 / v1.14.
