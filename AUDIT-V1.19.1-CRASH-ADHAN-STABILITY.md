# Noor Al-Iman — v1.19.1 Crash & Adhan Stability Patch

**Base branch:** `fix/v1.19-main-alarm-reliability`  
**Patch branch:** `fix/v1.19.1-crash-adhan-stability`  
**Scope:** WebView renderer recovery, runtime exit diagnostics, compass sensor threading, and exact-alarm permission recovery.  
**Release status:** Not released. Not merged into `main`.  
**Device validation:** Pending on a physical Samsung Android device.

## Why this patch exists

Samsung Device Care displayed the following warning:

> يتعطل نور الإيمان بشكل متكرر. ضعه في حالة السكون العميق للمحافظة على تشغيل هاتفك بسلاسة.

This warning is a symptom requiring investigation. It does not, by itself, prove a Java crash, WebView renderer crash, ANR, or a memory leak. The patch therefore adds evidence collection and prevents automatic renderer recovery from repeatedly recreating the Activity.

## Implemented changes

### 1. WebView renderer failure recovery — MainActivity.java

- Removed automatic `Activity.recreate()` from `onRenderProcessGone()`.
- The failed WebView is detached, its JavaScript interfaces are removed, and the WebView is destroyed.
- A native Arabic recovery screen is displayed with explicit **Retry** and **Close** actions.
- Retry is user-initiated; a renderer failure no longer triggers an automatic reload/recreate loop.
- Existing app data and preferences are not cleared by this recovery path.

### 2. Runtime and process-exit diagnostics — MainActivity.java

- Records renderer exit count, timestamp, `didCrash`, renderer priority, process PSS, available system memory, and low-memory state.
- On Android 11/API 30 and later, logs the recent `ApplicationExitInfo` records when the app starts, including reason, status, importance, timestamp, PSS/RSS, process name, and system description.
- Exposes a limited set of renderer diagnostics through the existing in-app alarm diagnostics JSON and displays renderer exit count, last exit time, and PSS in the diagnostics panel.
- These logs are diagnostic evidence; they do not independently identify the root cause of the Samsung Device Care warning.

### 3. Compass sensor processing — CompassBridge.java

- Replaced the main-looper sensor callback handler with a dedicated `HandlerThread` named `NoorCompassSensors`.
- Changed sampling from `SENSOR_DELAY_GAME` to `SENSOR_DELAY_UI` for rotation-vector, accelerometer, and magnetometer registrations.
- Added an explicit `shutdown()` lifecycle method to unregister the listener and stop the worker thread.
- MainActivity shuts down the compass worker when the WebView renderer fails and when the Activity is destroyed.

### 4. Exact-alarm permission recovery — MainActivity.java

- Made exact-alarm permission checking null-safe if AlarmManager is unavailable.
- On returning from Android settings, the Activity compares the current exact-alarm permission with the previously persisted state.
- If the permission changed, it logs the change and forces one prayer schedule rebuild.
- The existing JavaScript permission-status refresh remains in place.
- If the dedicated exact-alarm settings screen cannot be opened, the app falls back to Android app details settings.
- No new Android permission was added and no existing permission declaration was changed.

### 5. Validation checklist and CI

- Updated `TEST-CHECKLIST.md` to remove stale claims about inexact prayer-alarm fallback and to document renderer recovery, ApplicationExitInfo, compass threading, and permission round-trip tests.
- Added this audit note.
- Added the patch branch to the Android workflow push trigger so branch pushes run CI.

## Deliberately not changed

- `AndroidManifest.xml` permission declarations.
- Audio files or audio-selection behavior.
- Signing configuration, keystore, passwords, or signing identity.
- Dedication text and app identity.
- The 15-day prayer-alarm scheduling horizon.
- DeviceEventReceiver's manifest permission.
- The existing `MediaPlayer.setWakeMode(PARTIAL_WAKE_LOCK)` behavior.
- Timed dhikr scheduling logic (the earlier report's claim that `fire()` omitted `schedule(context)` was contradicted by the source code).

## Important engineering caveats

1. A native recovery screen prevents automatic recreate loops but does not prove that the renderer crash itself is fixed.
2. A 3.3 MB HTML source size does not prove a 3.3 MB memory footprint or establish the cause of renderer termination. Measure PSS/RSS and inspect exit records on the device.
3. `ApplicationExitInfo` reports process exits; a WebView renderer process termination may require separate Chromium/WebView logs and should not be conflated with the host app process exit.
4. Moving compass callbacks off the main looper reduces main-thread work, but the actual CPU/battery improvement must be measured.
5. Exact-alarm re-scheduling on permission return is implemented through Activity resume detection; it still requires a real-device permission grant/revoke test.
6. CI can validate compilation, unit tests, asset syntax, and APK signing. It cannot prove alarm delivery, foreground-service launch, audio output, Doze behavior, or Samsung power-management behavior.

## Required verification before release

- [ ] GitHub Actions passes on the latest branch head.
- [ ] Confirm no changes to Manifest permissions, audio assets, signing configuration, or dedication text.
- [ ] Trigger controlled WebView renderer loss and verify the native recovery screen appears without automatic recreation.
- [ ] Repeated renderer-loss test does not create an automatic retry loop.
- [ ] Inspect Android `ApplicationExitInfo` and Logcat after the Device Care warning.
- [ ] Measure app and renderer memory on the actual Samsung device.
- [ ] Test compass heading accuracy, lifecycle cleanup, and responsiveness on device.
- [ ] Revoke and grant exact-alarm access from system settings and verify schedule cancellation/rebuild behavior.
- [ ] Test adhan delivery and audio playback with screen off, Doze, Battery Saver, and Samsung background limits.
- [ ] Test the device with the app not placed in Deep Sleep; do not use Deep Sleep as a workaround for the warning.

**Decision:** `NOT READY FOR RELEASE` until CI passes and the required physical-device tests are completed. This branch is a stability patch candidate, not a published release.