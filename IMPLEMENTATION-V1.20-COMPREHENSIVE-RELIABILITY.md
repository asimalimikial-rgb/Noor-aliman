# Noor Al-Iman — Comprehensive reliability implementation

## Scope
This branch implements the approved reliability work without changing Android manifest permissions, package identity, minSdk/targetSdk, versionCode/versionName, signing configuration, audio assets, tafsir text, or dedication.

## Prayer scheduling
- Replaces the 15-day setExactAndAllowWhileIdle alarm fan-out with a single chained next event using AlarmManager.setAlarmClock().
- Persists the next event identity and an in-progress marker before scheduling so interrupted writes can be rebuilt on resume/boot.
- Uses a non-wakeup inexact midnight refresh for maintenance.
- Rebuilds the next prayer/reminder event after every delivered prayer event.
- Applies a five-minute late-adhan grace; stale adhan events are recorded and skipped.
- Reminder notifications calculate actual remaining minutes and are skipped after prayer time.

## Timed dhikr
- Uses AlarmManager.set(RTC_WAKEUP) rather than allow-while-idle alarms, avoiding competition with the time-critical prayer event.
- Uses a 90-minute delivery grace so Activity resumes do not cancel a deferred inexact alarm.
- Dhikr may be deferred by Doze; this is intentionally lower priority than prayer alarms.

## Audio
- Adds a 20-second MediaPlayer asynchronous preparation watchdog with cleanup and diagnostics.
- Makes the foreground notification Stop Adhan action always available.
- Removes fixed-duration native adhan preview timers. The preview UI follows the native service lifecycle and ends after MediaPlayer completion/service stop.

## WebView and local assets
- Replaces loadDataWithBaseURL() assembly of the multi-megabyte HTML document with a small local shell loaded through WebViewAssetLoader.
- Separates CSS, application JavaScript, tafsir UI JavaScript, compass JavaScript, and embedded tafsir JSON into local APK assets.
- Loads embedded tafsir JSON lazily on first tafsir use; it remains offline-first.
- Removes unsafe-inline from script-src because all app scripts are external local assets.
- Keeps allowFileAccess=false and allowContentAccess=false.
- Uses AndroidX WebKit 1.15.0, the stable release line compatible with minSdk 23.
- Adds a Web content revision key so saved state from the old data-URL document is not restored into the new asset shell.
- Makes IndexedDB page-cache writes best-effort so storage failure cannot block display of fetched Quran page data.

## Location defaults
- New installs default to Dubeiba/Meroe coordinates.
- Existing saved city/location preferences are preserved.

## Automated validation
- CI checks syntax for every external JavaScript file.
- CI verifies the app shell has no inline scripts and all local assets exist.
- CI checks prayer scheduling uses setAlarmClock and timed dhikr does not use allow-while-idle APIs.
- JUnit tests cover next-event selection, late-adhan policy, dynamic reminder timing, and media preparation timeout.

## Validation limits
GitHub Actions validates syntax, unit tests, build, and APK signature. It cannot prove real delivery under Samsung battery management, Doze, Force Stop, notification-channel settings, or physical speaker output. Those require physical-device testing. No Release is created by this branch.
