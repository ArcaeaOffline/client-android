# app

The Android application module: Compose UI, DataStore preferences, WorkManager jobs, and device integration.

Kotlin sources live under `src/main/java/`.

## Layout

```
xyz.sevive.arcaeaoffline/
├── ArcaeaOfflineApplication.kt   startKoin, Kermit file logging, Sentry
├── MainActivity.kt               launcher entry
├── MainScreen.kt                 hosts the NavigationSuiteScaffold
├── EmergencyModeActivity.kt      safe mode, launched via shortcut
├── OcrFromShareActivity.kt       share-target entry for OCR
├── ui/                           Compose UI (conventions below)
│   ├── activities/               entry activities' screens and ViewModels, including ocrfromshare/
│   ├── common/                   dialogs shared by several screens
│   ├── components/               composables used by 2+ features: dialogs/, ocr/, resources/
│   ├── helpers/                  formatters, display-chart cache
│   ├── navigation/               MainScreen, MainNavigationGraph, per-tab route enums
│   └── screens/<feature>/[<subfeature>/]
├── database/                     app-private Room 3 databases: AppDatabase, OcrQueueDatabase
├── datastore/                    DataStore stores and their repositories
├── data/                         Paths, BuildFlavor, maintenance tasks, notification channels
├── jobs/                         WorkManager workers
├── helpers/                      validators, OCR and resource helpers, diagnostics, activity/context extensions
├── di/AppModule.kt               the Koin module
├── logging/                      rolling file log writer
└── permissions/                  saving bitmaps to the gallery
```

## UI conventions

- Screens live in `ui/screens/<feature>/[<subfeature>/]`, and a screen shares its package with its ViewModel (e.g.
  `ui/screens/ocr/queue/OcrQueueScreen.kt` + `OcrQueueScreenViewModel.kt`).
- A screen file keeps orchestration only: state collection, effects, navigation, assembling subcomponents. A substantive component owned by
  one screen gets its own file under a `components/` directory in that feature's package, created when needed. Small single-use private
  helpers may stay in the screen file. Anything used by two or more features moves up to `ui/components/`.
- Navigation has two levels. `ui/navigation/Main.kt` holds the `MainScreen` enum (route, icon, title) and `MainNavigationGraph`. Subscreens
  are hosted behind a back-arrow scaffold by `ui/SubScreenContainer.kt`. Entry tabs pair an `XxxEntryScreen.kt` with an
  `XxxNavEntry.kt`, and `ui/AdaptiveEntryScreen.kt` supplies the list-detail navigation context.
- Theme and Arcaea-specific colors come from `:shared` (`ArcaeaOfflineTheme`, `LocalArcaeaColors`).

## Koin DI

Everything registers in `di/AppModule.kt`, which includes `coreModule` from `:core` and `thirdPartyModule`s. The module uses the Koin
compiler-plugin DSL: `single<T>()`, `single<Impl>().bind(Interface::class)`, `viewModel<T>()`, `worker<T>()`, and
`create(::factory)` for constructor functions. A new ViewModel, worker, or repository must be registered there.

## Data

- Room: `database/` holds `AppDatabase` and `OcrQueueDatabase` with `entities/`, `daos/`, `repositories/`, `converters/`. A repository is an
  interface plus its `*Impl` in the same file, and it exposes `Flow`. Schema JSONs export to `app/schemas/`. See also
  [`shared/AGENTS.md`](../shared/AGENTS.md) for bumping database versions.
- DataStore: one file per store, holding the data class, its `OkioSerializer`, and the `XxxPreferencesRepository`. The repository is the
  only read/write path.
- Workers: named `XxxJob` and registered with `worker<T>()`.

## Flavors

Dimension `app_stable` with `stable` and `unstable`; unstable adds `applicationIdSuffix = ".unstable"` and overrides launcher icons and
strings under `app/src/unstable/res/`. Runtime flavor checks go through `data/BuildFlavor.kt` (`BuildConfig.FLAVOR`).

## Tests

- `src/test/`: JVM unit tests with Robolectric available.
- `src/androidTest/`: instrumented tests, including a Compose UI test. They need a device or emulator, and the OCR tests need model assets —
  see the root `AGENTS.md`.
