# Pisacoma — Android

Native Android app (Kotlin + Jetpack Compose) of the Iglesia Adventista del Séptimo Día de
Pisacoma site, https://quiz-qwik.vercel.app: the same screens, content and design, with the
hymnals, Bible, Ellen G. White books and the Sabbath School quarterly bundled so it works offline
from the first open.

- Design: `docs/superpowers/specs/2026-10-13-android-app-design.md`
- Plan: `docs/superpowers/plans/2026-10-13-android-app.md`

## Install

Download the latest APK from **Releases** (or the `pisacoma-<run>` artifact of any green
**Actions → Build APK** run), open it on the phone and allow installing from that source.
Android 7.0 or newer.

## Screens

Inicio (the eleven animated scenes), Visítanos, Himnario (ES · PT antigo · NHA, with audio),
Videos, Escuela Sabática (quarter + lessons with notes, done marks and Bible popups), Biblia
(RVR1960), Diccionario, EGW (25 books), Quiz, ¿Quién?, Buscar. Notes, bookmarks, recents and quiz
progress are stored under the same keys the website uses in `localStorage`.

## Builds

Nothing is built on a PC. Every push runs the unit tests and builds a debug APK on GitHub Actions
(**Actions → Build APK**, artifact `pisacoma-<run>`). **Run workflow** by hand adds the Compose UI
tests on an emulator and uploads screenshots (`ui-<run>`). A `v*` tag builds the release APK and
publishes it under **Releases**.

The release and debug APKs are signed with one shared key kept only in the repository secrets
(`SIGNING_KEYSTORE` as base64, `SIGNING_STORE_PASSWORD`, `SIGNING_KEY_ALIAS`,
`SIGNING_KEY_PASSWORD`); without them a build uses a throwaway debug key.

## Content

`app/src/main/assets/` is generated from the site's repository by `tools/sync-content.mjs`, and
`app/src/main/res/font/` by `tools/fonts.mjs` (see `tools/README.md`). Re-run them when the site's
data changes (a new quarterly, new quizzes, new announcements) and commit the result; do not edit
the generated files by hand.
