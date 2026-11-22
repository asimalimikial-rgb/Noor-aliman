# Implementation Report — Noor Al-Iman v1.22.2

## Baseline and scope

- Repository: `asimalimikial-rgb/Noor-aliman`
- Base branch: `main`
- Baseline commit: `00f9f6fa444af8fb40f850d9751a01bc52944aa5`
- Baseline release: `v1.22.1`
- Target: maintenance release `v1.22.2` (`versionCode 28`)
- Work branch: `maintenance/v1.22.2-comprehensive`

All implementation changes are based on the exact v1.22.1 source in GitHub, not on the older uploaded v1.19.1 ZIP.

## Implemented changes

### 1. Dhikr playback service stability

File: `App/src/main/java/app/nooraliman/DhikrPlaybackService.java`

- Added a 20-second watchdog for asynchronous `MediaPlayer.prepareAsync()`.
- On timeout, the service cancels the watchdog, releases the player, abandons audio focus through the existing cleanup path, and stops itself.
- Cancels the timeout on prepared, completed, error, exception, and service cleanup paths.
- Ignores stale callbacks from a replaced player so an old preparation cannot cancel the new player's watchdog.
- Added a null check before creating the notification channel through `NotificationManager`.
- Added unit coverage for timeout boundaries and invalid timestamps in `DhikrPlaybackServiceTest`.

### 2. Compass bridge and sensor-thread safety

File: `App/src/main/java/app/nooraliman/CompassBridge.java`

- The baseline already declared `heading`, `declination`, and sensor-state flags as `volatile`; this was verified and preserved rather than redundantly changing the concurrency model.
- Replaced the deprecated display-rotation call on Android 11+ with `Activity.getDisplay()`.
- Retained a guarded legacy display lookup only for API levels below 30.
- No sensor registration, permission, or compass calculation policy was changed.

### 3. Tafsir UI DOM resilience

Files:
- `App/src/main/assets/scripts/tafsir-ui.js`
- `App/src/main/assets/noor-aliman.html`

- Replaced the brittle `#about .info-card:nth-child(2) p` selector with the stable `aboutOfflineInfo` element ID.
- Removed `innerHTML` from the tafsir attribution construction and created the link using DOM APIs and text nodes.
- Preserved the attribution URL, link target, and `rel=noopener` behavior.

### 4. Compass heading smoothing and calibration guidance

Files:
- `App/src/main/assets/scripts/compass.js`
- `App/src/main/assets/noor-aliman.html`

- Normalizes the smoothed heading after every interpolation step, preventing unbounded cumulative values while preserving shortest-angle interpolation.
- Added an explicit calibration-guidance control. It resets the display smoothing and instructs the user to move the phone slowly in a figure-eight pattern.
- This is user guidance, not a claim that software can physically recalibrate the device's magnetic sensor.

### 5. Battery restriction guidance

Files:
- `App/src/main/assets/noor-aliman.html`
- `App/src/main/assets/scripts/app.js`

- Added a contextual diagnostic explanation when Android reports that the app is not exempt from general battery optimization.
- Explains that Samsung, Xiaomi, and Oppo may have additional manufacturer-specific background/autostart controls.
- Retains the existing system battery-settings action; no battery exemption is silently requested and no new Android permission was added.
- The diagnostic does not claim to detect every OEM-specific restriction.

### 6. Independent Fajr sound and adhan volume

Files:
- `App/src/main/assets/noor-aliman.html`
- `App/src/main/assets/scripts/app.js`
- `App/src/main/java/app/nooraliman/MainActivity.java`
- `App/src/main/java/app/nooraliman/PrayerAlarmScheduler.java`
- `App/src/main/java/app/nooraliman/AdhanPlaybackService.java`

- Added an advanced setting for a Fajr-only sound, with a backward-compatible default of `same` (follow the general adhan sound).
- Added an independent adhan volume slider from 0% to 100%; default is 100% to preserve existing playback loudness.
- Sends both values through the existing WebView-to-native configuration bridge and stores them in `AlarmPrefs` (Device Protected Storage).
- Includes the Fajr sound and volume in the prayer schedule signature so changes invalidate and rebuild pending alarms.
- Applies the Fajr override only to Fajr playback; other prayers retain the general sound selection.
- Applies the configured gain using `MediaPlayer.setVolume()` without changing the phone's global media/alarm volume setting.
- Added distinct general and Fajr preview keys so the Fajr override cannot accidentally affect the general preview.
- Added volume-clamping unit tests, including NaN and infinity handling.

### 7. Quseir audio compatibility

Files:
- `App/src/main/java/app/nooraliman/MainActivity.java`
- `App/src/main/java/app/nooraliman/AdhanPlaybackService.java`
- `App/src/main/assets/scripts/app.js`
- `App/src/main/assets/noor-aliman.html`
- `AUDIO-LICENSES.md`

- The source inventory explicitly said to retain `adhan-quseir.mp3` for compatibility. Therefore, the file was not deleted.
- Instead, enabled the existing recording as a selectable sound, added the native asset mapping and preview support, and stopped silently migrating the legacy `quseir` preference to `classic`.
- Retained the existing audio-rights notes; this change does not claim new ownership or licensing rights.

### 8. Version, documentation, website, and CI

Files:
- `App/build.gradle`
- `README.md`
- `index.html`
- `.github/workflows/android.yml`

- Bumped `versionName` from `1.22.1` to `1.22.2` and `versionCode` from 27 to 28.
- Updated README release badges, links, and release notes.
- Updated website metadata, visible version labels, and direct APK links to `v1.22.2`.
- Added CI static assertions for the timeout, null guard, volatile heading, modern display API, stable DOM selector, calibration guidance, Fajr/volume controls, version metadata, and Quseir asset mapping.
- Added unit tests for the Dhikr preparation watchdog and adhan volume bounds.

## Protected configuration verified unchanged

- `AndroidManifest.xml`: not modified; existing permissions, receivers, services, Direct Boot declarations, and exported flags remain as in v1.22.1.
- Gradle release signing configuration: unchanged byte-for-byte.
- `applicationId`, `minSdk`, `targetSdk`, compile SDK, key alias, signing environment variable names, and keystore path: unchanged.
- No signing keys, passwords, or credentials were requested or exposed.
- Existing Exact Alarm, Direct Boot, alarm fallback, and receiver logic were not redesigned in this maintenance patch.

## Validation status

The code and documentation changes are prepared on the maintenance branch. Final acceptance still requires:

1. GitHub Actions build and all unit tests to pass on the pull request.
2. Successful release APK signing and signature verification by the existing workflow.
3. Review of the complete diff against the baseline.
4. After merge, creation of the `v1.22.2` release and verification that the APK and checksum assets are attached.
5. Manual device testing for Fajr/general sound selection, volume, full-length playback, locked-screen behavior, reboot/Direct Boot, Doze, and manufacturer battery controls.

CI cannot prove real-device alarm delivery under every OEM power-management policy. No manual device test is claimed as completed by this report.
