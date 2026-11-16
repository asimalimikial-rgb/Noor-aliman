# Noor Al-Iman v1.19.1 — Local stability patch notes

Base archive commit: `d45f244cacbaf2373221134f608e4b4ec46e5119`

## Changes applied

1. **Adhan playback failure visibility**
   - Added a shared failure-notification helper in `PrayerAlarmReceiver`.
   - Reused it for exact-alarm access failures and foreground-service launch failures.
   - Connected in-service failure paths (`audio-focus-denied`, asset/prepare failure, MediaPlayer error, and playback start error) to the same helper.
   - Preview playback is excluded.
   - Failure notifications are debounced per failure-state/prayer pair for five seconds, respect Android 13+ notification permission, and do not override a user-disabled notification channel.
   - Failure notifications use a dedicated `adhan_failure_v1` channel, separate from `prayer_reminders_v2`, so channel creation cannot suppress or lock in incorrect sound/vibration settings for ordinary prayer reminders. Users can independently disable the failure-notification channel.
   - The existing playback foreground-service notification channel and its sound/vibration settings were not changed.

2. **HTML asset loading memory path**
   - Replaced byte-array buffering plus decoded intermediate strings with UTF-8 character streaming into `StringBuilder`.
   - Performs local-city injection and enhancement-script insertion in the same HTML builder before the single final `toString()` required by `WebView.loadDataWithBaseURL`.
   - Does not use `openFd()` or assume the HTML asset is uncompressed.

3. **Exact-alarm permission UX**
   - Added an in-app explanation and direct settings action when Android 12+ exact-alarm access is absent and prayer adhan/reminder alerts are enabled.
   - Prompt is limited to once per 24 hours.
   - This dialog remains available even if Android notification permission is denied.

4. **Cancellation diagnostics**
   - Logs malformed/corrupt persisted alarm-ID data instead of silently swallowing the parsing/cancellation exception.
   - Existing cancellation and preference-clearing behavior was intentionally preserved.

5. **Device-event comments/logging**
   - Corrected comments to distinguish the exact-alarm grant broadcast from revocation behavior.
   - Clarified that prayer alarms have no inexact fallback, while dhikr has its own fallback.

## Deliberately not changed

- `WebView.pauseTimers()` / `resumeTimers()` were not added. The page has a `requestAnimationFrame` playback loop that advances the audio playlist, plus timers for UI and other functions. Pausing all WebView timers while backgrounded could interrupt playlist progression. This needs a targeted background-audio test before changing lifecycle behavior.
- No Manifest permissions/components, signing configuration, version metadata, Gradle configuration, audio assets, XERJ-related files, release workflows, or release settings were changed.
- No Git commit, push, merge, GitHub Release, or APK publication was performed.

## Validation status

- JavaScript syntax: passed for all executable inline scripts in `noor-aliman.html` and for `enhancements.js` using Node.js `--check`.
- Java source parser scan: no Java syntax/parser diagnostics found. Full compilation was not possible because Android SDK platform jars are not installed in this environment.
- Gradle build/unit tests: blocked. Gradle wrapper attempted to download Gradle 8.13 but network DNS access to `services.gradle.org` was unavailable (`UnknownHostException`).
- Device/runtime playback, notification-permission, exact-alarm, Doze, and WebView memory tests: not run.
