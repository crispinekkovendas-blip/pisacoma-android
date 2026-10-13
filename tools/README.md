# tools

`npm install` once, then:

- `node sync-content.mjs` — copies the site content into two generated roots (set `SITE=` to
  the quiz-qwik checkout if it is not at the default path):
  - `app/src/main/assets/` — bundled in the APK: home posters + Lottie, EGW covers, the small
    indices, licences, and a copy of the manifest;
  - `content/` — downloaded by the app file by file from raw.githubusercontent.com, listed in
    `content/manifest.json` as `{ path, bytes, pack }` (packs: `himnario-es`, `himnario-pt`,
    `biblia`, `escuela`, `buscar`, `egw`). Text files are written with LF endings and git stores
    the folder verbatim (`.gitattributes`), so the byte counts match what the phones download.
- `node fonts.mjs` — converts the site woff2 fonts to `app/src/main/res/font/*.ttf`.
