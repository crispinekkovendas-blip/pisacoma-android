# tools

`npm install` once, then:

- `node sync-content.mjs` — copies the site content into `app/src/main/assets` (set `SITE=` to the quiz-qwik checkout if it is not at the default path).
- `node fonts.mjs` — converts the site woff2 fonts to `app/src/main/res/font/*.ttf`.
