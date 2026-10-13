# SDD ledger — plan: docs/superpowers/plans/2026-10-13-content-downloads.md

Spec: docs/superpowers/specs/2026-10-13-content-downloads-design.md (read).
Executor: inline (executing-plans), no subagents (user rule). Every RED/GREEN is a GitHub Actions run.

Pre-flight (shared interfaces):
- T1 → T2: manifest JSON shape `{generated, files[{path,bytes,pack}]}` consumed by `Manifest.parse` — consistent.
- T2 → T3/T4: `Manifest.byPath`, `filesFor(mode)`, `ContentMode.persists/downloads` — consistent.
- T3 → T4/T5/T6: `ContentStore(bundled, dir, manifest, mode, fetch)`, `missing: MutableStateFlow<String?>`, `download(path)`, `isStored` — consistent.
- T4 → T5/T7: `ContentDownloader(store, scope, onComplete)`, `start(mode): Job?`, `state`, `missingFor` — consistent.
- T5 → T6/T7/T8: `App.content`, `App.downloader`, `Keys.contentMode/contentPending`, `ContentTestRunner`, `TestState.seedContent/wipeContent` — consistent.
- T6 → T7: `Route.Descargas` is created in T6 (placeholder screen) and filled in T7 — ruled in the plan text.
Ruling: the user's message is a direct build request and the session runs autonomously, so the spec was not held for a review round; decisions are recorded here — cost if wrong: a rework of the chooser copy or pack split after the user sees it.
Task 1: RED run 38463423449 → 113 tests, 16 failed (ManifestFilesTest ×5, AssetsPresentTest ×11: content missing) — matches Expected.
Task 1: Ruling: CompositeAssetSource + TestAssets (planned for Task 5) folded into Task 1 — ModelsParseTest/SearchTest read the moved files and the suite must be green at every task boundary — cost if wrong: none (Task 5 just has less to do).
Task 1: fix run 38464344331 compiled main but the unit-test compile failed on ManifestTest (Task 2's RED, committed early by `git add -A`). Ruling: Task 1 GREEN and Task 2 GREEN are proven by the same run (Manifest.kt push) — cost if wrong: one combined evidence run instead of two.
Task 2: RED run 38464344331 → e: Unresolved reference 'Manifest' / 'ContentMode' / 'formatMb' in ManifestTest.kt — matches Expected (compile failure in the test source set).
Task 1: complete (commits 7b1faf4..f6dfa38 + 7c8ab5c, tests: CI run 38464614154 build → BUILD SUCCESSFUL, unit suite green incl. ManifestFilesTest, AssetsPresentTest, CompositeAssetSourceTest)
Task 2: complete (commits 7c8ab5c, tests: CI run 38464614154 → BUILD SUCCESSFUL incl. ManifestTest 11/11)
Task 3: RED run 38464865876 → Unresolved reference 'ContentStore' in ContentStoreTest.kt — matches Expected.
Task 3: complete (commit aefcefd, tests: CI run 38465182965 → BUILD SUCCESSFUL incl. ContentStoreTest 10/10)
Task 4: RED run 38465599694 → Unresolved reference 'ContentDownloader' / 'DownloadState' — matches Expected.
Task 4: complete (commit 8af44ad, tests: CI run 38465951308 → success incl. ContentDownloaderTest 7/7 and ContentStoreTest 11/11)
Task 4: Ruling: ContentStore.download() no longer raises the missing-content notice (only read() does) — a background download failing must not pop a notice over whatever screen the user is on — cost if wrong: none visible (the Descargas screen shows its own error state).
Task 5+6: Ruling: executed as one RED/GREEN cycle — Task 5 (wiring) has no unit test of its own and App.kt points ContentErrors.report at the store, so the two compile only together — cost if wrong: one combined evidence run.
Task 5+6: RED run 38466281554 → Unresolved reference 'ContentErrors' in ContentErrorsTest.kt — matches Expected.
Task 7: Ruling: GREEN for Tasks 5, 6 and 7 lands in one push — AppNavHost registers the bare /descargas destination (Task 7) in the same file that wires the reload and Route.Descargas (Task 6), so they compile only together; Task 7's tests are pushed RED first — cost if wrong: a failure in the combined run needs one diagnosis step more.
Task 7: RED run 38466571797 → unit-test compile fails on DescargasViewModel / Route.Descargas (see above) — matches Expected.
Task 5+6+7: run 38466843760 → main, unit-test and androidTest sources compile; 156 tests, 1 failed: DescargasViewModelTest.chooseEsencialDownloadsAndClearsPending (race: Done published before onComplete cleared pendiente). Fix: onComplete before the final state. New RED: ContentStoreTest.staleFileIsRefetched (isStored must compare the size with the manifest so a new APK re-downloads changed files).
Task 7: run 38467264702 → 157 tests, 1 failed: staleFileIsRefetched (the RED for the size check); DescargasViewModelTest all green after the onComplete ordering fix.
Task 5: complete (commits 009b478..fcabf3c, tests: CI run 38467635036 build → BUILD SUCCESSFUL; main, unit-test and androidTest sources compile; ContentTestRunner wired)
Task 6: complete (same commits/run; ContentErrorsTest 3/3; every viewModelScope.launch carries ContentErrors; notice in SiteScaffold)
Task 7: complete (same commits/run; RoutesTest + DescargasViewModelTest 6/6 green; 157 unit tests total)
Task 8: emulator run 38467645872 dispatched on fcabf3c (DescargasTest ×5 + the 23 existing tests over the seeded store).
Task 8: ui run 38467645872 → 28 tests, 1 failed: offlineMissingContentShowsTheNoticeAndLeadsToDescargas (Juan 3 was already in BibleRepository's in-process cache from BibleTest, so no read reached the store). Fix: the test opens Apocalipsis 1. Screenshots descargas-primera-vez / descargas-error / descargas-uso-elegido reviewed: match the spec; eyebrow changed to «— Guardar en el teléfono» (the screen is about saving, not an outage).
Task 8: ui run 38468620358 → same test failed; cause: MainActivity parsed Uri.path only, so a deep link's query string was dropped (/biblia?libro=66 → default Juan 3, cached). Fixed in MainActivity (path + query); this also fixes the site's query-carrying deep links.
Task 8: complete (commits 9177bba..6214041, tests: ui run 38469492888 → 28/28 UI tests green, 31 screenshots in artifact ui-69; descargas-primera-vez, descargas-error, descargas-uso-elegido, sin-conexion-biblia, descargas-desde-aviso reviewed against the spec)
Final review: self-review (no subagent tool — user rule: inline only). Review Focus: 1 offline first run → DescargasTest.offlineFirstRunShowsTheErrorState; 2 resume → ContentDownloaderTest.skipsStoredFiles + App.onCreate pending flag (not device-tested); 3 en-linea never writes → ContentStoreTest.onlineModeNeverWrites; 4 deep link on first open → DescargasTest.firstRunContinuesToTheDeepLink (+ the Uri.query fix it uncovered); 5 short body → ContentStoreTest.rejectsShortBody.
Final: minor (deferred): DescargasViewModel.build() walks the store directory (≤100 files) on every progress event, on the main thread.
Final: minor (deferred): «Descargar todo» (29 MB) has no Wi-Fi-only option or mobile-data warning.
Final: minor (deferred): the resume-on-next-open of an interrupted download is unit-tested (skip stored files) but not exercised on a device.
Final: minor (deferred): the manifest's `generated` stamp is unused; freshness relies on the size check, so a changed file with the same byte count would not be re-downloaded.
Task 9: docs (README «Descargas», tools/README), ledger copied to docs/, tag v0.2.0.
