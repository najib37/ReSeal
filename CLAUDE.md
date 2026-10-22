# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

Seal is an Android video/audio downloader (Jetpack Compose, Material 3 Expressive) built on yt-dlp via
`youtubedl-android`. This repo is a fork of JunkFood02/Seal published as `najib37/ReSeal`.

## Commands

The dev machine is low-spec: run **one Gradle build at a time** (check for a running build first if another
session may be active). `gradle.properties` already compiles Kotlin in-process with 2 workers; don't undo that.

```bash
./gradlew assembleGenericDebug                    # debug APKs -> app/build/outputs/apk/generic/debug/
./gradlew testGenericDebugUnitTest                # JVM unit tests (app/src/test)
./gradlew testGenericDebugUnitTest --tests 'com.junkfood.seal.util.ParsingTest'   # one class
./gradlew ktfmtFormat                             # formatter (ktfmt, kotlinLangStyle); ktfmtCheck to verify
adb install -r app/build/outputs/apk/generic/debug/Seal-*-generic-arm64-v8a-debug.apk
```

- Flavors: `generic` (default), `githubPreview` (`.preview` app id), `fdroid`. Debug builds use app id
  `com.junkfood.seal.debug`, so they install next to the official app.
- APK ABIs come from `ABI_FILTERS` in `gradle.properties`; release signing reads `keystore.properties` if present.
- Unit tests exist only for pure helpers (output templates, URL/filename/cookie parsing). UI work is verified on a
  device: install, drive with `adb shell input` / `uiautomator dump`, take `screencap` screenshots. Before any tap or
  screenshot, confirm Seal is the focused window (`dumpsys window | rg mCurrentFocus`); the phone is the user's own.
  Don't install while `DownloadService` is running (`dumpsys activity services com.junkfood.seal.debug`); it kills
  active downloads.

## Version constraints (don't "upgrade" past these without a migration)

- **AGP 8.x / compileSdk 36.** Newer androidx core, Compose 1.12, navigation 2.10 and OkHttp 5.5 require AGP 9.1 +
  compileSdk 37; versions in `gradle/libs.versions.toml` are pinned to the last AGP-8-compatible releases.
- **`material3` 1.5.0-alpha18** is pinned separately from the Compose BOM: it's the newest build with the
  Expressive APIs (`MaterialExpressiveTheme`, `LoadingIndicator`, `ToggleButton`, `ButtonGroupDefaults`,
  `LargeFlexibleTopAppBar`, `ListItemDefaults.SegmentedGap`) that still builds with AGP 8.
  `ExperimentalMaterial3ExpressiveApi` is opted into module-wide in `app/build.gradle.kts`.
- MMKV is pinned to 1.3.x for 32-bit support. Coil stays on 2.x (Coil 3 changes packages).

## Architecture

**Modules:** `:app`, `:color` (`com.kyant.monet`, Monet tonal palettes behind the theme), `buildSrc` (version).

**Download pipeline (the part that spans many files):**
- `DownloaderV2` (`download/`, a Koin singleton from `App.kt`) owns a `SnapshotStateMap<Task, Task.State>` that every
  queue/history UI observes. A task goes `Idle → FetchingInfo → ReadyWithInfo → Running → Completed | Error | Canceled`;
  `doYourWork()` picks the next task (max 3 concurrent), holds a wake lock and starts `DownloadService` while work runs,
  and refuses to run on metered networks unless `CELLULAR_DOWNLOAD` is on.
- `util/DownloadUtil` builds every `YoutubeDLRequest`. Two paths matter:
  `fetchVideoInfoFromUrl` (probe, `--dump-single-json`) and `downloadVideo`. The raw probe JSON is kept in
  `InfoJsonCache` (keyed by extractor+id, 2h TTL) and the download runs with `--load-info-json`, so one link is
  extracted once; a failed cached run retries from the URL.
- All yt-dlp executions go through `withCache()`: youtubedl-android otherwise adds `--no-cache-dir`.
- Format choice is expressed as yt-dlp `-S` sort strings (`toFormatSorter()`, or `sortingFields` with
  `formatSorting = true`), not hard-coded format ids, except when the user picks formats in Custom.
- Task-level actions (cancel/resume/delete/share/open) are handled by `rememberTaskActionHandler` in
  `ui/page/downloadv2/DownloadPageV2.kt`, shared by the queue and the Downloads (history) page.

**Download UI flow** (`ui/page/downloadv2/configure/`): link sheet (`InputUrlDialog.kt`) → configure sheet
(`DownloadDialogV2.kt`, driven by `DownloadDialogViewModel` actions) offering **Fast download** (inline
`QualitySlider`s, 360p–4K + audio level, enqueues immediately via `toFastDownload()`) or **Custom** (fetches info, then
the full `FormatPage`). Format page selection rules keep video-only, audio-only and muxed formats from being mixed.

**History vs queue:** the Downloads page (`ui/page/videolist/VideoListPage.kt`) merges the history DB (rows are written
only when a download completes) with in-progress tasks from `DownloaderV2`, filtered by status chips.

**Preferences:** MMKV via `util/PreferenceUtil` (string keys + typed defaults maps); `DownloadPreferences` is the
snapshot passed into each task.

## UI conventions

- Settings rows must use the shared segment components in `ui/component/PreferenceSegment.kt`
  (`PreferenceDefaults`, `PreferenceSegment`, `PreferenceGroup`): Pixel-Settings-style segmented groups, 16dp screen
  margin, tonal icon circles. Wrap related rows in `PreferenceGroup(title = …)`; don't add per-page padding or text sizes.
- Full-screen pages use `SealLargeTopAppBar`; bottom sheets use `SealModalBottomSheet` / `SlideUpPage`
  (no Material 2 sheets). Single-choice button groups use `ConnectedChoiceRow`.
- Fix spacing/typography in the shared component, not with per-screen hard-coded values.

## Workflow in this repo

- Several Claude sessions may work here at once. Do UI/feature work in a git worktree (e.g. `../Seal-ui`) and
  coordinate builds with other sessions.
- Branches follow upstream naming (`feat/…`, `fix/…`, `build/…`, `perf/…`); changes land via `gh pr create` +
  `gh pr merge --merge` against `najib37/ReSeal`.
- SSH to GitHub (port 22 and ssh.github.com:443) is blocked here; push over HTTPS with gh credentials:
  `git -c credential.helper= -c 'credential.helper=!gh auth git-credential' push https://github.com/najib37/ReSeal.git <branch>`.
