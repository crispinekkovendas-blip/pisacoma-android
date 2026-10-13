# Pisacoma — Android

Native Android app (Kotlin + Jetpack Compose) of the Iglesia Adventista del Séptimo Día de
Pisacoma site, https://quiz-qwik.vercel.app: the same screens, content and design. The first
open asks what to keep on the phone (everything, the essentials, only what gets used, or nothing)
so the hymnals, Bible, Ellen G. White books and the Sabbath School quarterly read offline.

- Design: `docs/superpowers/specs/2026-10-13-android-app-design.md`, `docs/superpowers/specs/2026-10-13-content-downloads-design.md`
- Plans: `docs/superpowers/plans/2026-10-13-android-app.md`, `docs/superpowers/plans/2026-10-13-content-downloads.md`

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

## Descargas (what the phone keeps)

The heavy content is not in the APK. It lives in `content/` of this repository and the app
downloads it file by file from `raw.githubusercontent.com/…/main/content/` (the URL is
`CONTENT_BASE_URL` in `app/build.gradle.kts`), described by `content/manifest.json` (path, bytes,
pack). The APK bundles only the home, the EGW covers and the small indices.

The first open shows `/descargas` and nothing else until one of four modes is chosen (pref
`contenido.modo`); the menu's «Descargas» row reopens it to change the choice, complete a
download or delete what is stored.

| mode | up front | on every read |
|---|---|---|
| Descargar todo (`todo`) | every pack, about 29 MB | bundled → disk → network, saved to disk |
| Descargar lo esencial (`esencial`) | Himnario, Biblia, Escuela Sabática + Quiz, búsqueda, about 6 MB | same; an EGW book is saved the first time it is opened |
| Solo lo que uso (`uso`) | nothing | same: saved the first time it is used |
| Mantener solo en línea (`en-linea`) | nothing | bundled → network; nothing written |

A download interrupted by closing the app resumes on the next open (`contenido.pendiente`). A
file that is neither stored nor reachable shows one «Sin conexión» notice over the screen with
«Reintentar» and «Descargas».

## Content

`content/` and `app/src/main/assets/` are generated from the site's repository by
`tools/sync-content.mjs`, and `app/src/main/res/font/` by `tools/fonts.mjs` (see
`tools/README.md`). Re-run them when the site's data changes (a new quarterly, new quizzes, new
announcements), commit the result and push. Phones fetch files they lack on the first read, and
a file is counted as stored only with the size the APK's manifest expects, so a new release makes
changed files download again. Do not edit the generated files by hand.
