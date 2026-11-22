# Noor Al-Iman v1.22.0 — Implementation and Validation Report

## Base and scope
- Base: main at e0c0f4c5fb3ba8337fe0d4fa15f528a6d19364a4 (v1.21.1).
- Candidate branch: feat/v1.22-pro-settings-stability.
- Uploaded v1.19.1 ZIP treated as historical reference, not replacement source.
- No Android permission declarations, signing identity, keystore, or audio asset was changed.

## Implemented
- Simple default settings view and opt-in professional mode with search.
- Diagnostics for exact alarms, notifications, full-screen intent, battery optimization status, and last alarm scheduling state.
- Direct system-settings actions and a background adhan preview action.
- Existing prayer method, Asr mode, per-prayer minute adjustments, dhikr settings, sound choices, and element IDs preserved.
- Five mobile navigation destinations; Tasbeeh remains reachable from Adhkar. About and licensing remain accessible from Settings.
- Active page persistence includes Settings.
- Full Screen Intent dialog, UI-thread navigation, null-safe status check, and app-details fallback.
- MediaPlayer timeout cleanup, null-safe full-screen notification check, and Audio Focus preparation guard.
- Lock-screen adhan screen handles new intents, back-button stop, and reusable drawing Path.
- Stop bar CSS corrected for RTL centering and mobile safe-area spacing.

## Deliberately not implemented
- Direct Boot (LOCKED_BOOT_COMPLETED) is not enabled. Alarm preferences remain in credential-protected SharedPreferences; registering the event without migrating and validating preferences to device-protected storage would falsely imply pre-unlock scheduling.
- Timed dhikr remains an inexact non-allow-while-idle alarm by design; frequent allow-while-idle alarms risk Doze quota throttling.
- A separate Fajr-only sound selector was not exposed because Android currently accepts one global sound selection. Doha already selects a different bundled recording for each of the five prayer keys.
- No new Android permissions were added.

## Validation
- Pending CI: JavaScript syntax checks, Android unit tests, Release build, APK signature verification.
- Physical-device QA remains required for exact-alarm access, notification permission, full-screen intent, Doze, battery management, reboot, lock screen, and full-length adhan playback.
