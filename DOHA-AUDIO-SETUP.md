# Doha adhan recordings — current source state

All five prayer-specific Doha recordings are already present in:

`App/src/main/assets/`

| Prayer | Exact filename |
|---|---|
| Fajr | `Adhan_Doha_Qatar_01_Fajr_Adhan.mp3` |
| Dhuhr | `Adhan_Doha_Qatar_02_Dhuhr_Adhan.mp3` |
| Asr | `Adhan_Doha_Qatar_03_Asr_Adhan.mp3` |
| Maghrib | `Adhan_Doha_Qatar_04_Maghrib_Adhan.mp3` |
| Isha | `Adhan_Doha_Qatar_05_Isha_Adhan.mp3` |

The playback service maps each prayer to its matching file. No manual addition is currently required for these five recordings.

## Rights and attribution

The source collection is:
https://archive.org/details/adhan.recordings.from.doha.qatar

The collection page displays a Public Domain Mark 1.0 notice, but the uploader is listed as Unknown. This metadata does not independently establish ownership or redistribution rights. Keep the attribution and rights caveat in `AUDIO-LICENSES.md`; do not claim that the recordings are conclusively rights-cleared without verification.

## Validation after a new build

- Confirm the five files are packaged in the APK.
- Play each prayer-specific recording from the sound preview.
- Confirm that each prayer uses its own file, not the Fajr fallback.
- Confirm the APK audio remains decodable.
