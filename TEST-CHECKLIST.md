# Noor Al-Iman v1.16 candidate — device test checklist

This checklist describes the current five-recording source state and the v1.16 reliability changes. Source checks and CI builds do not replace testing on a physical Android device.

## Install, upgrade, and lifecycle
- [ ] Install the candidate over v1.15 using the same signing key; confirm Android accepts the upgrade and saved preferences remain.
- [ ] Reboot the phone; confirm enabled prayer alarms and timed dhikr are scheduled again.
- [ ] Change device time and timezone; confirm schedules refresh.
- [ ] Update/reinstall with app data preserved; confirm package-replaced handling rebuilds schedules.
- [ ] Rotate the screen; confirm WebView state and current page are retained.
- [ ] Use Android Back from Settings/About/Quran; confirm page history behaves correctly.

## Settings navigation and presentation
- [ ] Open Settings from the persistent top-bar gear shortcut on both small and large screens.
- [ ] Confirm Settings opens inside the app page flow, not as a separate dialog/window.
- [ ] Verify all settings remain present in their grouped cards: location/calculation, adhan/alerts, adhkar/tasbeeh, Quran/Qibla, appearance/language, and prayer adjustments.
- [ ] Change each representative control, leave Settings, return, and confirm values persist and existing behavior still works.
- [ ] Verify the shortcut has an accessible label, visible focus state, and adequate touch target; confirm removing Settings from the crowded mobile bottom bar does not hide access.

## Prayer times and adjustments
- [ ] Compare displayed prayer times with a trusted local timetable for several dates and locations.
- [ ] Test every calculation method and both Standard/Hanafi Asr settings.
- [ ] Test locations at high latitudes: when a solar altitude is unreachable, confirm the UI shows no calculated time and Android does not schedule a fabricated noon/midnight alarm; document the limitation and any supported adjustment behavior.
- [ ] Change each prayer offset by +1 and -1 minute; verify display and native alarms use the same adjusted time.
- [ ] Reset one prayer and all prayer offsets; verify both UI and scheduled alarms update.
- [ ] Verify Ramadan/Isha boundary dates against the selected calculation method and a trusted calendar.

## Prayer alerts and adhan
- [ ] With exact-alarm permission denied, verify the inexact fallback event is delivered and the diagnostics label it approximate; measure real delay on the target Android device.
- [ ] Revoke exact-alarm access while the app is backgrounded, then reopen it; verify the fallback schedule is rebuilt and the UI explains possible delay.
- [ ] Grant exact-alarm access again; verify prayer and dhikr schedules return to exact mode.
- [ ] Play each adhan recording repeatedly; verify asynchronous preparation succeeds and no descriptor/resource leak appears after stop, completion, or error.
- [ ] With exact-alarm permission granted, verify each enabled prayer alarm at a test time.
- [ ] On Android 12+, deny/revoke exact-alarm access while prayer alerts are enabled; confirm prayer alarms are still scheduled through the inexact fallback (allow for Android delivery delay).
- [ ] Reopen/resume the app while exact-alarm access remains denied; confirm prayer fallback alarms are re-created rather than skipped.
- [ ] Restore exact-alarm access; confirm the next schedule uses exact alarms again.
- [ ] Verify the app does not request or change any permission as part of the fallback path.
- [ ] Verify notification permission granted and denied paths.
- [ ] With full-screen-intent access granted, lock the phone and verify the adhan alert activity appears.
- [ ] With full-screen-intent access denied, verify the ordinary notification still appears and opens the app.
- [ ] Stop playback from the notification, in-app stop bar, and full-screen alert.
- [ ] Let the full adhan finish naturally; verify the full-screen alert closes and the display no longer stays awake.
- [ ] Verify the stop action closes the alert even when playback is stopped from the notification.
- [ ] Verify status/navigation bar hiding and portrait/landscape layout on supported Android versions.
- [ ] Enable/disable volume-key stop and verify expected behavior.
- [ ] Enable/disable face-down stop and verify sensor behavior.
- [ ] Enable/disable screen-off stop; while adhan is playing, turn the screen off and confirm playback stops only when the option is enabled.
- [ ] Start another audio source, then trigger adhan/dhikr; verify audio focus is acquired and released correctly.

## Bundled audio and licensing
- [ ] Test each bundled sound: classic, Aqib, short, Azemmour, Makkah, and Doha.
- [ ] Select Doha and verify each prayer uses its matching Fajr, Dhuhr, Asr, Maghrib, or Isha recording.
- [ ] Verify the preview ends on the actual media completion event, not a fixed estimated duration.
- [ ] Confirm audio assets decode without errors.
- [ ] Review attribution and redistribution rights for every third-party recording before public redistribution. Doha collection uploader is Unknown; its Public Domain Mark notice is not independent proof of ownership or rights.

## Offline, Quran, and data
- [ ] First-launch offline behavior: verify the Quran page explains that its page data may need an initial network fetch.
- [ ] Download a surah while online, then test playback offline.
- [ ] Verify local tafsir, adhkar, prayer sounds, and saved settings offline.
- [ ] Test download cancellation, retry, low-storage behavior, and app backgrounding.
- [ ] Test persistence and recovery of bookmarks, downloads, and settings after process death.

## WebView, accessibility, and performance
- [ ] Background and resume the app repeatedly; verify the WebView remains responsive and user state is retained.
- [ ] Exercise renderer-loss recovery (where test tooling permits); verify a fresh WebView is created without clearing local settings or downloads.
- [ ] Test on a device/emulator without compass sensors; verify the app reports unavailable compass instead of crashing.
- [ ] Verify the published website HTML begins with `<!doctype html>` as the first non-whitespace content and renders in standards mode.
- [ ] Verify local app fonts and Quran page layout with network disabled; compare Arabic glyph shaping and line breaks.
- [ ] Test camera/location permission flows and confirm untrusted origins are denied.
- [ ] Verify only HTTP/HTTPS external links open; unsupported schemes remain blocked.
- [ ] Test navigation and touch targets on small-screen devices and larger font/display scaling.
- [ ] Confirm timers, compass polling, and animation loops pause when their feature/page is inactive.
- [ ] Test Android 6, 13, 14, 15, and 16 where available.

## Build and release integrity
- [ ] Confirm GitHub Actions runs unit-test task and signed release build on the candidate branch.
- [ ] Confirm APK signature verifies and matches the established release signing identity.
- [ ] Verify APK checksum against the uploaded checksum file.
- [ ] Confirm README and website download links resolve to the exact release asset filename.
- [ ] Do not publish a new GitHub Release or merge to main until review and physical-device checks are complete.
- [ ] Never commit keystore files, signing passwords, or tokens.

## v1.17 visual-system regression checks
- [ ] Verify the new responsive visual layer loads in the Android WebView after the existing enhancement script.
- [ ] Check home, Quran, Qibla, adhkar, tasbeeh, About, and every Settings group on narrow and wide screens.
- [ ] Check light and dark themes; confirm text, select controls, cards, and adjustment rows retain readable contrast.
- [ ] Navigate all controls by keyboard/accessibility focus and verify visible focus indicators and touch targets.
- [ ] Enable Android's reduced-motion preference where supported; confirm transitions do not interfere with navigation.
- [ ] Confirm the persistent adhan stop bar remains reachable above Android system navigation insets.
- [ ] Verify all prior saved settings, prayer adjustments, downloads, bookmarks, and audio selections survive upgrade from v1.16.1.

## v1.17 automated checks
- [ ] Run Node syntax validation on `App/src/main/assets/enhancements.js`.
- [ ] Verify the public website starts with an HTML doctype and the Android Release APK builds and passes signature verification.
- [ ] Note: no `src/test` or `src/androidTest` source sets currently exist; a successful Gradle test task alone does not demonstrate feature-level unit or device coverage.
