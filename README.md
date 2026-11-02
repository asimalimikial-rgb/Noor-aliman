# Noor Al Iman for Android

- Application ID: `app.nooraliman`
- Version: `1.12` (`versionCode` 14)
- Minimum Android version: Android 6.0 (API 23)
- Compile SDK: Android 15 (API 35)

Version 1.12 adds in-app adhan stop controls, an independent playback-notice
setting, and saved per-prayer minute adjustments. Version 1.11 added in-page
reading/recitation controls, swipe and nearby Mushaf page-turn buttons, and two
additional offline adhan recordings. Audio sources and licenses are listed in
`AUDIO-LICENSES.md`.

Open this folder in Android Studio and let Gradle sync, or build from a terminal:

```sh
./gradlew assembleDebug
```

The debug APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

Release signing needs the existing private key and passwords. Keep them out of
GitHub and provide them through environment variables or the CI secret store:

```sh
export NOOR_ALIMAN_KEYSTORE=/secure/path/noor-aliman-release.jks
export NOOR_ALIMAN_STORE_PASSWORD=...
export NOOR_ALIMAN_KEY_PASSWORD=...
./gradlew assembleRelease
```

The private key and signing information are not part of this source project.
Do not replace the key if you need to publish updates for existing installs.

The offline dhikr clips, bundled salawat recording, and their license details
are documented in `AUDIO-LICENSES.md`.

The Qibla camera view requests camera access only after it is opened and stops
the stream when closed. Android plays the bundled Arabic salawat recording.
Its source and redistribution license could not be verified; see
`AUDIO-LICENSES.md` before distributing the app more widely.
