# Manual source update — 2026-11-09

## v1.18.0 recovery candidate — not yet released

- Add WebView pause/resume handling and renderer-loss recovery without clearing user data.
- Keep the adhan AssetFileDescriptor open until asynchronous MediaPlayer preparation completes; close it on success and all error/destruction paths.
- Preserve prayer and timed-dhikr alarms when exact-alarm permission is unavailable by using Android's inexact allow-while-idle fallback; expose actual delivery precision in diagnostics and explain possible delays in Settings.
- Rebuild schedules after exact-alarm permission changes, including revocation.
- Guard compass startup on devices without an available SensorManager.
- Serialize branch CI runs to prevent repeated pushes from creating competing Android builds.

## Validation

- This candidate is isolated on branch fix/webview-background-recovery; main and the published v1.18.0 release are unchanged.
- GitHub Actions run 130 passed unit tests, signed Release APK build, and APK signature verification for the alarm/WebView changes before the final small UI-copy and compass-guard commits.
- Run 131 is the current validation for the complete branch state.
- Physical-device checks remain required for background/renderer recovery, exact-alarm permission revoke/grant, inexact delivery delay, all adhan recordings, and compass hardware.
- Inexact alarms may be delayed by Android and may not be suitable for guaranteed on-time adhan audio. Exact-alarm permission remains recommended for accurate prayer-time playback.
