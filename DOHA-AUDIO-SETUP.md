# Adding the missing Doha adhan recordings

The app already includes the Doha Fajr, Dhuhr, and Maghrib recordings. The app owner supplies the Asr and Isha MP3 files manually.

## Exact destination

Copy both files into:

`App/src/main/assets/`

This is the same folder that already contains `Adhan_Doha_Qatar_01_Fajr_Adhan.mp3`.

## Required filenames

| Prayer | Exact filename |
| --- | --- |
| Asr | `Adhan_Doha_Qatar_03_Asr_Adhan.mp3` |
| Isha | `Adhan_Doha_Qatar_05_Isha_Adhan.mp3` |

Keep the spelling, capitalization, underscores, and `.mp3` extension exactly as shown. If your source files have different names, rename copies of them to these names before adding them.

## Important

- Put the files directly in `App/src/main/assets/`, not in `res/raw/`, a nested folder, or the repository root.
- Do not remove or replace the existing Fajr, Dhuhr, or Maghrib files.
- Until the new files are present in a build, Asr and Isha temporarily use the existing Doha Fajr recording as a fallback.
- After adding the files, build a new APK. Adding files to the source folder does not update an APK that was already built.
- The source collection's rights metadata has not been independently verified; check that you have permission to use and redistribute the files in the app.

## Quick folder check

The folder should contain all five files:

- `Adhan_Doha_Qatar_01_Fajr_Adhan.mp3`
- `Adhan_Doha_Qatar_02_Dhuhr_Adhan.mp3`
- `Adhan_Doha_Qatar_03_Asr_Adhan.mp3`
- `Adhan_Doha_Qatar_04_Maghrib_Adhan.mp3`
- `Adhan_Doha_Qatar_05_Isha_Adhan.mp3`
