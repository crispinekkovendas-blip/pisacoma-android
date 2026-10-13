# Pisacoma Android app — design spec

**Date:** 2026-10-13 · **Status:** draft for review · **Source of truth being mirrored:** `C:\Users\admin\sda-church-nextjs\quiz-qwik` (Qwik City site, live at https://quiz-qwik.vercel.app)

## 1. Intent

Build a **native Android app in Kotlin** that is a faithful copy of the Qwik site: the same screens, the same content, the same design tokens, the same behaviours, so a member of the Pisacoma church gets the website experience as an installed app that works without signal from the first open. The user's words: *"build android app, exactly following the website the qwik website"* → *"Rebuild natively inline in Kotlin"*.

**Who it is for:** the congregation in Pisacoma (Aymara/Spanish speakers, mid-range Android phones, patchy mobile data at 3,900 m). **Success:** every site route has an app screen that looks and behaves the same on a phone; hymnals, Bible, EGW books and the current quarterly open with no network; a release APK installs on a real phone.

Not in scope: iOS, tablets/landscape layouts beyond what the site does, Google Play publishing (needs the $25 developer account, which needs the user's explicit yes), the retired Next.js site, the existing Expo app (left untouched).

## 2. Decisions taken as assumptions (correct any before the plan)

| # | question | decision | why |
|---|---|---|---|
| 1 | Minimum Android | **minSdk 24 (Android 7.0), targetSdk 36** | covers effectively every phone still in use in Peru; Compose, Media3 and Lottie all support 24 |
| 2 | Content | **bundled in the APK** (hymnals, Bible, 25 EGW books, current quarterly, quizzes, search index, covers, home posters + Lottie) | offline from first open; this is the whole point of an app over the PWA. ~43 MB raw, ~28 MB APK after compression |
| 3 | Live data | fetched at runtime with graceful empty states: YouTube video feeds (through the site's `/videos/api/youtube`), Wiktionary definitions, the announcements sheet, hymn MP3 streams (through the site's `/api/audio` proxy), NHA hymn audio and all videos (YouTube) | same sources the site uses; nothing new to host |
| 4 | Home motion | **same Lottie files** played with `lottie-compose`, portrait or landscape JSON chosen by orientation, one pause button remembered like the site | exact parity; the JSONs already exist |
| 5 | Audio | **Media3 ExoPlayer** with a foreground `MediaSessionService` so a hymn keeps playing with the screen off and shows in the notification shade | native expectation for an audio app |
| 6 | YouTube | `android-youtube-player` (WebView wrapper of the IFrame API) inside the app's own HBO-style controls, matching the site's custom player | YouTube terms require the IFrame player; the library gives native-feeling controls |
| 7 | Persistence | **SharedPreferences/DataStore with the site's localStorage key names verbatim** (`biblia:bookmarks`, `egw:<slug>:ch<n>:notes`, `escuela:<QID>:lecc<n>:day<d>:done`, `quiz.<slug>.v1`, `himnario.recent`, `inicio.videos.pausa`, …) | parity in behaviour, and a future import/export between site and app stays trivial |
| 8 | Fonts | Fraunces, Inter, DM Sans converted once from the site's woff2 to TTF (wawoff2 is already in `quiz-qwik/motion/node_modules`) and bundled in `res/font` | Android has no woff2 font loader |
| 9 | Publishing | **signed release APK built locally**, installed by file/ADB; no Play account | purchase needs an explicit yes |
| 10 | Where it lives | project at **`E:\pisacoma-android`** (junction `C:\Users\admin\sda-church-android` → it), `GRADLE_USER_HOME=E:\gradle-home`, emulator AVDs on E:; JDK = Android Studio's bundled `jbr` (OpenJDK 21) | C: has 13 GB free; Gradle + AGP caches alone are 1–2 GB |

## 3. Architecture

Single-activity **Jetpack Compose** app, **Navigation Compose** with one destination per site route and the same path shapes, so deep links `https://quiz-qwik.vercel.app/<route>` can later map 1:1.

```
app/
  src/main/
    AndroidManifest.xml            single MainActivity + HymnPlaybackService
    assets/                        content copied from the site by a script (never edited by hand)
      data/himnario-{es,pt,pt-novo}.json
      data/biblia/{1..66}.json  data/bible-books.json
      data/egw/{25 slugs}.json  covers/{slug}.jpg
      data/escuela-sabatica-2026-03.json  data/quizzes.json
      data/search-index.json  data/church.json
      home/{id}-{landscape,portrait}.jpg  home/lottie/*.json + img/
    res/font/{fraunces,fraunces_italic,inter,dm_sans}.ttf
    res/drawable/sda_symbol*.xml   the official symbol as vector drawables (plain + circle)
  java/pe/pisacoma/iglesia/
    MainActivity.kt                sets content, edge-to-edge, handles orientation
    App.kt                         Application: repositories, player connection
    nav/Routes.kt                  sealed routes, mirrors site paths
    nav/AppNavHost.kt
    ui/theme/Tokens.kt             every colour/spacing/radius from the site, verbatim, by the site's names
    ui/theme/Type.kt               Fraunces / Inter / DM Sans scales
    ui/theme/Theme.kt              PastelTheme (cream) · DarkTheme (quiz, quien, videos) · HomeTheme (Pampa palette)
    ui/chrome/                     TopBar (brand mark + hamburger + search), FullMenu, Footer
    ui/components/                 TypesetIndex (editorial list), NotesPanel, TextSizeControls, BibleRefPopup, ShareSheet
    feature/home/                  HomeScreen, HomeScene, ScenesMotion (lottie-compose), SabbathTimes
    feature/visitanos/
    feature/himnario/              HymnalIndexScreen, HymnScreen, HymnProjectorScreen, player UI
    feature/videos/                VideosScreen (hero, rows, search, channel chips), VideoPlayerScreen
    feature/escuela/               QuarterScreen, LessonScreen (7 day tabs, markdown, notes, done, bookmarks)
    feature/biblia/                BibleScreen (book/chapter picker, text size, notes, bookmarks, recent, Aymara link)
    feature/diccionario/
    feature/egw/                   EgwCatalogScreen (4 rails), EgwBookScreen (chapters, reader, notes, done, last)
    feature/quiz/                  QuizIndexScreen, QuizScreen (commit-answer flow, levels)
    feature/quien/                 WheelScreen
    feature/buscar/                SearchScreen (same ranking as utils/search.ts)
    data/                          one repository per content type reading assets lazily (Json via kotlinx.serialization)
    data/prefs/UserState.kt        notes/done/bookmarks/recent with the site's key names
    data/net/                      YouTubeFeed, Wiktionary, Anuncios (OkHttp + kotlinx.serialization), with timeouts and empty states
    audio/HymnPlaybackService.kt   Media3 MediaSessionService
    util/Zmanim.kt                 NOAA port of utils/zmanim.ts (sunset, Sabbath start/end)
    util/BibleRefs.kt              port of bibleStore.ts findRefs (Spanish book names, ranges)
    util/Markdown.kt               lesson markdown → AnnotatedString with clickable Bible refs
tools/
  sync-content.mjs                 copies the site's data + assets into app/src/main/assets, and evaluates the TS-only
                                   sources (quizzes/*.ts, church.ts, anuncios.ts, featured-hymns.ts) to JSON with tsx;
                                   run whenever the site's content changes
  fonts.mjs                        woff2 → ttf
docs/superpowers/specs/            this file
```

### Data flow

- **Bundled content** is read from `assets/` on first use by a repository (one per type), parsed with kotlinx.serialization on `Dispatchers.IO`, and kept in memory for the process lifetime. EGW books (21 MB total) are parsed one book at a time, like the site fetches one JSON per book.
- **User state** goes through `UserState`, a thin wrapper over SharedPreferences exposing `Flow`s, keyed exactly as the site's localStorage.
- **Live data** goes through small `data/net` clients with 8 s timeouts. Failure never blocks a screen: videos show the "sin conexión" state the site shows, the dictionary shows the site's error line, announcements simply don't render.
- **Audio**: a screen asks `HymnPlaybackService` (via `MediaController`) to play `https://quiz-qwik.vercel.app/api/audio?url=<mp3>`; the service owns ExoPlayer, the notification and audio focus. NHA hymns (`ytId`) and videos go through the YouTube player instead.

### Theming rule

`Tokens.kt` carries the site's values under the site's names (`--cream #f5f3e6`, `--sage #4a6741`, `--forest #2c4128`, `--ink #1f2b1c`, Pampa `basalt #2a2620`, `parchment #f0e8d0`, `olive #5d6b3f`, `ichu #7d8a5a`, `olive-pale #d4d6b0`, `ochre #c08428`, `ochre-light #d9b070`, `ochre-deep #8a5418`, `titicaca #1c5d8e`, …). Any new value found while porting a screen is added there with its CSS name; nothing is eyeballed. Mobile rules from the site carry over: no hover states, pressed states on every tappable, 44 dp minimum targets.

## 4. Screens (parity checklist)

Each app screen must match the site's phone layout at 390 px. Items are the site's features today, taken from the route files.

1. **Inicio `/`** — eleven full-height scenes (`inicio, himnario, videos, escuela, biblia, diccionario [light], egw, quiz, quien, [anuncios band], sabado, noche`): poster image → Lottie (portrait/landscape) → veil → title, one sentence, one action. Live: current lesson number, this week's Friday/Saturday sunset from `Zmanim.kt`, schedule, announcements. Fixed pause button (`inicio.videos.pausa`). Night scene: stars Lottie, the symbol, photo credits. Never suppressed by the system's reduce-motion setting.
2. **Visítanos** — sections *Horarios* (SCHEDULE), *Dónde estamos* (map tile with a pin, "abrir en Maps" intent, altitude/coords), *Tu primer sábado*, *A qué pertenecemos* (HIERARCHY), *Nuestra historia* (TIMELINE, PIONEERS); "Añadir al calendario" shares the site's `/calendario.ics` URL or inserts the events via the calendar intent.
3. **Himnario** — three-way toggle ES (613) · PT antigo (610) · NHA (601); typeset editorial index with chapter breaks and picks column (FEATURED_HYMNS); search by number, title and lyric line; recent hymns (`himnario.recent`).
4. **Himno `/himnario/<variant>/<n>`** — title, stanzas/chorus typeset like the site, Bible reference line, prev/next, audio player (ES: Google Drive MP3 + instrumental when present; PT: Hina7 MP3; NHA: YouTube), **Proyectar** worship mode (full-screen stanza pager, dark), Compartir (Android share sheet with the site URL), records the visit.
5. **Videos** — hero, rows per category and per channel (4 channels), channel chips, search, relative dates in Spanish; feed from the site's YouTube API with cache; **player** full screen with the site's custom controls (play/pause, seek, time, speed, fullscreen) over the IFrame player; related rows below.
6. **Escuela Sabática** — quarter header (title, dates, author, introduction), all 13 lessons with per-lesson progress dots, the current lesson highlighted by date; quarter rollover message when the bundled quarterly has ended ("Nuevo trimestre").
7. **Lección** — 7 day tabs (Sábado–Viernes) + the memory verse; markdown rendered to native text; Bible references tappable → bottom-sheet with the verses from the bundled RVR1960; per-day *done* and notes, lesson bookmarks, last day remembered; text size control; link to the lesson's quiz when one exists.
8. **Biblia** — book picker (66, OT/NT), chapter grid, verse text with numerals, prev/next, text size, notes per chapter, bookmarks, recent chapters, "Leer en aimara" external link.
9. **Diccionario** — search word → Wiktionary definitions (same parse), suggested words, clear button, error line when not found/offline.
10. **EGW** — catalogue: 25 covers in 4 rails (fundacionales, devocionales, práctica adventista, tiempos finales) with the site's intro; **book**: chapter drawer, reader with text size, chapter done + notes, bookmarks, "continuar donde quedaste" (`egw:<slug>:last`).
11. **Quiz** — index of quiz packs (lessons 4, 5, 6, 12, 13 today), the week's quiz first; **quiz view**: question, four options, pick → "¿Confirmar esta respuesta?" → reveal + explanation, progress, levels to Caleb, result; state in `quiz.<slug>.v1`.
12. **¿Quién?** — name wheel (dark theme): add names (comma/line separated), remove, clear, spin with easing, winner card, "Cómo usarla".
13. **Buscar** — one field over the bundled search index: hymns (title + first lines), EGW chapters, lessons, Bible references typed as text ("Juan 3:16"), same ranking and row layout; opened from the top bar's search button.
14. **Chrome** — top bar: brand mark (official symbol) + «Pisacoma / Iglesia Adventista del Séptimo Día», search, hamburger → full-screen menu with the 11 entries, descriptions and icons, symbol in the menu footer; footer on cream screens.

Not ported: `/offline`, `/inicio-clasico`, `/404`, the service worker, `/contacto` (a redirect).

## 5. Error handling

- Asset JSON is trusted and parsed strictly; a parse failure is a build bug, surfaced by a unit test that loads every asset.
- Network: timeouts and non-2xx become typed `Result` failures; each screen has its site-equivalent empty/error state and a "Reintentar" action. Nothing is cached to disk except the videos feed (24 h, DataStore) so the Videos screen opens offline with the last list.
- Audio: ExoPlayer errors show the site's "No se pudo cargar el audio" line and stop the service.
- Storage: SharedPreferences writes are fire-and-forget; reads default to empty.

## 6. Testing

- **Unit (JVM):** `Zmanim.kt` against the site's known Pisacoma sunset values; `BibleRefs.kt` against the cases in `bibleStore.ts`; `Search` ranking against `utils/search.ts` examples; markdown → refs; every asset JSON deserialises.
- **Compose UI tests** on the emulator for the flows that carry state: quiz commit-answer, lesson done/notes persistence, hymn toggle + search, wheel add/remove.
- **Parity check per screen:** side by side screenshots, emulator (Medium Phone, API 36.1 AVD already present) vs the site at 390 px, before a screen is called done.
- **Release:** `./gradlew assembleRelease` with a local keystore on E:, install on the user's phone via ADB.

## 7. Build order (for the implementation plan)

1. Scaffold on E: with Gradle wrapper, Kotlin 2.x, AGP 8.x, Compose BOM, `GRADLE_USER_HOME` on E:, JDK = `jbr`; empty app boots on the AVD.
2. `tools/sync-content.mjs` + `tools/fonts.mjs`; theme tokens; chrome (top bar, menu, footer); Navigation skeleton with placeholder screens.
3. Content repositories + `UserState` + unit tests.
4. Screens in nav order, each to parity: Himnario → Himno (+ audio service) → Escuela → Lección (+ Bible refs) → Biblia → EGW → Quiz → Quién → Buscar → Visítanos → Diccionario → Videos (+ player) → Inicio (Lottie) last, since it depends on live bits from the others.
5. Release signing, APK on the phone, vault + memory update.
