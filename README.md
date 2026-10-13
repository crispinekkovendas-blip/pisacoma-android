# Pisacoma — Android

Native Android app (Kotlin + Jetpack Compose) of the Iglesia Adventista del Séptimo Día de
Pisacoma site, https://quiz-qwik.vercel.app: the same screens, content and design, with the
hymnals, Bible, Ellen G. White books and the Sabbath School quarterly bundled so it works offline.

- Design: `docs/superpowers/specs/2026-10-13-android-app-design.md`
- Plan: `docs/superpowers/plans/2026-10-13-android-app.md`

## Builds

Every push runs the unit tests and builds a debug APK on GitHub Actions (**Actions → Build APK**,
artifact `pisacoma-<run>`). A `v*` tag publishes a release APK under **Releases**.
Nothing needs to be built locally.

## Content

`app/src/main/assets/` is generated from the site's repository by `tools/sync-content.mjs`;
do not edit it by hand.
