# Manual source update — 2026-11-03

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
- A real Android Gradle build and device tests are not claimed by this source-only update.
