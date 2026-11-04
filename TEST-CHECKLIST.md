# Noor Al-Iman v1.14 — Device Test Checklist

Run these checks after building and installing the APK. A source-only review does not replace device testing.

## Install and lifecycle
- [ ] Install v1.13 over the installed v1.12 build using the same signing key; confirm Android accepts the upgrade and saved preferences remain.
- [ ] Reboot the phone; confirm enabled prayer alarms and timed dhikr are scheduled again.
- [ ] Change device time and timezone; confirm schedules refresh.
- [ ] Update/reinstall the package with data preserved; confirm `MY_PACKAGE_REPLACED` rebuilds schedules.
- [ ] Rotate the screen and trigger a configuration change; confirm the current WebView page/state is retained.
- [ ] Use Android Back from Settings/About/Quran; confirm page history returns to the previous page, then Home, then exits.

## Prayer alerts and sound
- [ ] With exact-alarm permission granted, verify each enabled prayer alarm at a test time.
- [ ] Revoke exact-alarm access; confirm the app does not crash and schedules resume after access is restored.
- [ ] Verify notification permission granted and denied paths.
- [ ] With full-screen-intent access granted, lock the phone and verify the adhan alert activity appears.
- [ ] With full-screen-intent access denied, verify the ordinary notification still appears and opens the app.
- [ ] Verify the stop action works from the notification, the in-app stop bar, and the full-screen alert.
- [ ] Verify the adhan alert hides status/navigation bars and fills the display on supported Android versions.
- [ ] Enable volume-key stop; verify either volume key stops playback while the fullscreen adhan alert is visible.
- [ ] Disable volume-key stop; verify volume keys retain their normal volume behavior.
- [ ] Confirm volume-key stop is not promised when Android suppresses the fullscreen activity.
- [ ] Enable face-down stop; place the phone screen-down during playback and verify it stops after the debounce interval.
- [ ] Disable face-down stop; verify the accelerometer no longer stops playback.
- [ ] Start another audio source, then trigger adhan/dhikr; verify audio focus is acquired and released correctly.
- [ ] Test each bundled adhan asset and the original salawat recording.
- [ ] Select Doha adhan and verify Fajr uses the Doha Fajr recording, Dhuhr uses the Doha Dhuhr recording, and Maghrib uses the Doha Maghrib recording.
- [ ] Verify Asr and Isha use the Doha Fajr recording as fallback; do not label this as prayer-specific audio.
- [ ] Verify the Doha preview plays the Fajr recording.
- [ ] Confirm the About screen links to the Doha Internet Archive collection and clearly states that ownership/redistribution rights have not been independently verified.

## Fullscreen adhan alert
- [ ] Verify the mosque illustration, deep-green/gold/cream palette, prayer name, and large stop button in portrait and landscape.
- [ ] Verify tapping stop ends playback and closes the alert.
- [ ] Verify the alert does not keep the display awake after playback ends.

## WebView, permissions, and build
- [ ] Test location and camera permissions from the trusted app page; confirm untrusted origins are denied.
- [ ] Verify links open only HTTP/HTTPS URLs externally; file/content/javascript/unknown schemes are blocked.
- [ ] Verify Qibla, Quran downloads/streaming, tafsir, and offline content still work under the Content Security Policy.
- [ ] Test layout and navigation on Android 6, Android 13, Android 14, Android 15, and Android 16 where available.
- [ ] Build both Debug and minified Release variants; inspect R8 output and confirm all WebView bridge methods still work.
- [ ] Confirm the release APK is signed with the same private keystore as v1.12. Never commit the keystore or passwords.
