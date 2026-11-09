# Noor Al-Iman — v1.18 Adhan & Prayer Alarm Reliability Audit

**Base:** `main` at `fe72ac19de8be69129abb54b2bbdcd0bade1f450` (v1.17.0 / versionCode 20)  
**Working branch:** `fix/v1.18-adhan-scheduler-audit`  
**Status:** Changes implemented on the audit branch; not merged or released.

## Confirmed defects

### 1. Inexact alarms were used for user-facing prayer alerts
When Android 12+ exact-alarm access was absent, the scheduler fell back to `setAndAllowWhileIdle()` for both adhan playback and the 10-minute reminder. Android documents that inexact alarms may be delayed (on Android 12+, commonly up to one hour absent additional power restrictions). Exact alarms are exempt from foreground-service launch restrictions; inexact alarms are not. The receiver then attempted to start the adhan foreground service from the background, which could be rejected and leave the adhan silent.

**Fix:** Prayer alerts now use `setExactAndAllowWhileIdle()` only when exact-alarm access is available. No inexact fallback is used for adhan or prayer reminders. The daily schedule-maintenance refresh remains inexact because it is not user-facing. Settings now show exact-alarm access status and provide a direct settings action. If access is absent, the app explains why automatic prayer alerts are not scheduled rather than silently promising an inaccurate alert.

### 2. Pending alarms could be cancelled at a prayer boundary
The WebView calls the native configuration bridge when `renderTimes()` runs. The countdown calls `renderTimes()` when the current prayer time is reached. The old scheduler cancelled and rebuilt all pending alarms on every call; if that happened before Android delivered the due broadcast, the just-due alarm could be cancelled and then skipped by the future-time guard.

**Fix:** The scheduler now calculates a stable signature from location, timezone, calculation method, Asr method, alert toggles, sound and manual offsets. Unchanged settings with a valid future daily refresh no longer cancel or rebuild alarms. Explicit events (boot, clock/timezone changes, permission grants, and daily refresh) force a rebuild.

### 3. Activity resume caused unnecessary rescheduling
The old `onResume()` rebuilt prayer alarms every time the activity returned to the foreground, including ordinary pauses and returns from system UI.

**Fix:** Rebuild on the first activity resume and when exact-alarm permission state changes; ordinary resumes no longer cancel pending alarms.

### 4. Umm al-Qura Ramadan boundary mismatch
The WebView uses the `islamic-umalqura` calendar through `Intl`, while Android previously used an arithmetic Hijri approximation. These can disagree on Ramadan boundaries, causing the native Isha alarm to differ from the displayed Isha time by 30 minutes.

**Fix:** Android API 24+ now uses ICU `IslamicCalendar.CalculationType.ISLAMIC_UMALQURA`, with the previous arithmetic method retained only as a guarded fallback for API 23 or ICU failure.

### 5. Playback and receiver failures were insufficiently observable
The alarm receiver did not guard/report foreground-service start failures, and the playback service did not preserve useful diagnostics for audio-focus denial, media preparation errors or player errors.

**Fix:** Receiver launch failures are caught and logged, with a fallback notification when notification permission permits. Local diagnostics now record expected versus actual alarm delivery time, delay, alarm type/prayer, playback result, selected sound and error details. A settings row displays the last recorded result.

### 6. Northern Sudan location preset missing
The city list contained Khartoum, Makkah, Cairo, Istanbul and GPS only; Khartoum was the fallback. A Dibeiba/Merowe preset is now injected before the main WebView script executes, so it is available even when the saved city is already Dibeiba. It does not change the user's saved city or global default:
- Latitude: 18.45352
- Longitude: 31.81315
- Time zone: Africa/Khartoum
- Method default for this preset: Umm al-Qura

Users can still choose GPS or another city. Coordinates are a locality-level map reference, not a substitute for device GPS.

## Regression coverage

- Existing Asr-angle tests retained.
- Added exact-alarm permission gating tests.
- Added idempotent scheduler decision tests (unchanged signature, changed signature, expired daily refresh, forced rebuild).
- Existing face-down sensor tests retained.
- Current source contains 13 JUnit tests across the two test classes, including boundary tests that ensure an alarm one millisecond ahead is not discarded.

## Automated verification

The first CI run after the alarm, permission, scheduler, diagnostics and test changes completed successfully:
- Web asset validation: passed
- Java 17 / Android SDK 36 setup: passed
- `clean testDebugUnitTest assembleRelease`: passed
- APK signature verification: passed
- Signed Release APK artifact upload: passed

A subsequent run was triggered after adding the Dibeiba/Merowe location preset; its result must be checked before considering this branch verified.

## Required real-device QA (not yet completed)

1. On Android 12+, enable **Alarms & reminders** for Noor Al-Iman and confirm the app status changes to enabled.
2. Enable notification permission; verify both adhan playback and the 10-minute reminder.
3. Test with screen off, app swiped away, Doze/battery saver and Samsung battery management settings.
4. Verify each of the five prayer alarms against the displayed time; check the local diagnostic delay.
5. Revoke and re-grant exact-alarm access; confirm automatic rescheduling.
6. Reboot, change device time/timezone, and verify schedule reconstruction.
7. Test DND, media playback already in progress, audio-focus loss, each adhan recording, and the face-down stop option.
8. Compare the Dibeiba/Merowe preset with GPS and a locally trusted prayer timetable.

## Protected project assets

- No Android permissions were added or removed; only an intent action was added to the existing device-event receiver.
- No audio asset was changed.
- No signing configuration or key was changed.
- No dedication text was changed.

## Release gate

This branch is not merged into `main`, and no new GitHub Release has been published. Device QA remains necessary before treating the changes as fully validated for end users.
