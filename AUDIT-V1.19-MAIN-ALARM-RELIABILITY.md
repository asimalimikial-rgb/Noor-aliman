# Noor Al-Iman — v1.19 Main-branch Alarm Reliability Patch

**Source of truth:** `main` at `ba7baf79510c8a7e05326bdefb559063a76e850e`  
**Patch branch:** `fix/v1.19-main-alarm-reliability`  
**Scope:** Prayer alarm precision, timed dhikr idempotency, notification permission recovery and regression coverage.  
**Status:** Implemented on the patch branch; GitHub Actions run #154 passed (unit-test task, Release build and APK signature verification). Physical-device QA is still pending. This is not a claim of a completed end-to-end device audit.

## Confirmed issues on main

1. `PrayerAlarmScheduler` scheduled user-facing prayer adhan and reminders with `setAndAllowWhileIdle()` when exact-alarm permission was absent, and retried an exact-alarm `SecurityException` using the same inexact fallback.
2. `PrayerAlarmReceiver` accepted events marked as inexact and could still attempt to start adhan playback from a background broadcast.
3. `DhikrAlarmScheduler.schedule()` cancelled its existing PendingIntent on every call, including ordinary activity resumes, increasing the risk of cancelling an alarm at its delivery boundary.
4. `MainActivity` persisted a one-time notification permission request flag. After denial, there was no in-app direct route to Android app notification settings.

## Patch behavior

### Prayer alerts
- User-facing adhan and prayer reminders are now scheduled only when exact-alarm access is available.
- No inexact fallback is attempted when permission is missing or Android rejects an exact alarm.
- The daily schedule-maintenance refresh remains inexact because it is not itself a prayer notification.
- The scheduler records a diagnostic state: `exact_permission_required`, `scheduled_exact`, `partial_schedule_failure`, `schedule_failed`, `no_future_alarms`, or `alarm_manager_unavailable`.
- On Android 12+, the receiver ignores legacy inexact adhan events and refuses playback if exact-alarm access has been revoked.
- Settings copy now explains that prayer alerts are not scheduled until exact-alarm access is enabled.

### Timed dhikr
- A stable signature and stored next-fire timestamp are used to preserve an unchanged pending alarm.
- The scheduler rebuilds only when settings change, the PendingIntent is missing, or the stored schedule is stale beyond a short delivery grace period.
- The receiver clears consumed schedule metadata before creating the next interval.
- Regression tests cover unchanged schedules, changed settings, missing PendingIntents and delivery-boundary behavior.

### Notification permission
- The Android bridge exposes notification permission state and opens the app's system settings page.
- The settings UI shows whether notifications are allowed and provides a direct system-settings action after denial.
- The app does not repeatedly prompt on every resume, and it does not clear app data or alter permission declarations.

## Regression coverage added or updated
- Exact-alarm permission gating for prayer alerts.
- Timed-dhikr idempotency and delivery-boundary behavior.
- Device checklist updated to remove the obsolete claim that prayer alerts fall back to approximate alarms.
- Android CI workflow now includes this patch branch.

## Protected items
- No Android Manifest permission declarations were added, removed or changed.
- No audio recordings or playback assets were changed.
- No signing configuration, keystore, passwords or signing identity were changed.
- No dedication/الإهداء text was changed.
- No release was published and `main` was not modified directly.

## Automated validation completed
GitHub Actions run [#154](https://github.com/asimalimikial-rgb/Noor-aliman/actions/runs/43036894314) completed successfully:
- Web asset validation passed.
- Java 17 / Android SDK 36 setup passed.
- `clean testDebugUnitTest assembleRelease` passed.
- APK signature verification passed (one signer; v1 and v2 verification true).
- Signed APK and SHA-256 artifact uploaded as `noor-al-iman-v1.19.0-signed-apk`.
- Release publishing jobs were skipped as expected because this is a pull request, not a push to `main` with a publish commit.

## Validation still required
1. Physical-device QA:
2. Android 12+: revoke/grant exact-alarm permission and verify no inexact prayer alerts are scheduled.
3. Screen-off/background/Doze and Samsung battery management tests.
4. Notification permission denied/granted settings round-trip.
5. Repeated app resume and timed-dhikr delivery-boundary tests.
6. Compare all five prayer alarm timestamps with displayed times and inspect actual delivery-delay diagnostics.

A successful CI run validates compilation and automated tests only. It does not establish that Android delivered every alarm or started audio on a physical device.
