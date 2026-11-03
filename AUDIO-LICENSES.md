# Audio Licenses

The three recorded dhikr clips are bundled both as Android raw resources and as
embedded WAV data in the WebView page. The salawat MP3 is the original recording
provided by the app owner, who confirmed that they made the recording. The same
recording is retained in both `assets/salawat.mp3` and `res/raw/salawat.mp3` for
the existing offline playback paths.

## Offline adhan recordings

- `adhan-azemmour.mp3`: `muezzin_call.wav` by Gvido, recorded in Azemmour,
  Morocco. Freesound preview MP3; licensed under Creative Commons Attribution
  3.0. Credit: Gvido / Freesound.
  Source: https://freesound.org/people/Gvido/sounds/36997/
  License: https://creativecommons.org/licenses/by/3.0/
- `adhan-makkah.mp3`: “Adhan, Great Mosque of Mecca - Jan 21, 2013” by
  Seyfula Islam. Source file was WebM (VP8/Vorbis), converted to MP3, stereo,
  48 kHz, 192 kbps for offline playback. The Commons file page identifies
  the work as CC BY 3.0 Unported. Credit: Seyfula Islam.
  Source: https://commons.wikimedia.org/wiki/File:Adhan,_Great_Mosque_of_Mecca_-_Jan_21,_2013.webm
  License: https://creativecommons.org/licenses/by/3.0/
  Changes: container/codec conversion from WebM/Vorbis to MP3. The source page
  does not identify a specific muezzin; no claim is made that this is a verified
  official live broadcast from inside Masjid al-Haram.

The Quseir / Red Sea recording (`adhan-quseir.mp3`) was removed from the app
assets and selectable sound list in this A11 working copy, as requested.

## Doha recordings planned for A11 (not yet bundled)

The selected Fajr, Dhuhr, and Maghrib MP3 files from the Internet Archive item
“Adhan Recordings from Doha, Qatar” have not yet been downloaded or added to
this source archive. The item page displays a Public Domain Mark 1.0 notice,
but the uploader is listed as Unknown; this notice is source metadata, not
independent verification of ownership. Do not describe these three files as
included until the actual files and source metadata have been checked.
Collection: https://archive.org/details/adhan.recordings.from.doha.qatar

## Subhan Allah, Alhamdulillah, Allahu Akbar

- Source: Audio.com, uploader `hasibahmed868`, "Tasbeeh (subhanallah), Tahmeed
  (alhamdulillah), Tahleel (La ilaha illallah), Takbeer (Allahu Akbar)"
- Source page: https://audio.com/hasibahmed868/audio/tasbeeh-subhanallah-tahmeed-alhamdulillah-tahleel-la-ilaha-illallah-takbeer-allahu-akbar
- License: CC0 1.0 Universal, https://creativecommons.org/publicdomain/zero/1.0/
- Excerpts: Subhan Allah 1.55-2.95 s; Alhamdulillah 4.25-5.90 s; Allahu Akbar
  10.90-12.55 s. These excerpts were converted to mono, 48 kHz, 16-bit PCM WAV.

## Salawat phrase

- Phrase: "صَلُّوا عَلَى رَسُولِ اللَّهِ".
- Source: restored from an earlier user-provided app HTML file.
- Provenance: original recording made by the app owner, as confirmed by the owner.
- Redistribution: owner-provided original recording; retain this attribution note
  when redistributing the app.

## Quran recitations

- Al-Zain Mohamed Ahmed and Noreen Mohamed Siddiq are streamed as individual
  ayah files from https://cdn.mualim.app/ using the reciter entries in
  https://huggingface.co/datasets/zaibihassan/Quranic-Recitation-Data.
- The dataset page states Apache-2.0 for the dataset. That listing does not
  establish ownership of the underlying recitation recordings; recordings
  remain subject to the rights and terms of their respective reciters and
  providers.
- Alafasy and Al-Sudais recordings are streamed from Quran.com; Maher Al-Muaiqly
  and Mohamed AbdulKareem are streamed from EveryAyah.

## Doha recordings planned for A11 (not yet bundled)

The selected Fajr, Dhuhr, and Maghrib MP3 files from the Internet Archive item
“Adhan Recordings from Doha, Qatar” have not been downloaded or added to this
source archive yet. The item page displays a Public Domain Mark 1.0 notice,
but the uploader is listed as Unknown; the notice is recorded as source metadata
and is not independent verification of ownership. Do not describe these three
files as included until the actual files and source metadata have been checked.
Collection: https://archive.org/details/adhan.recordings.from.doha.qatar
