# :shared

The Kotlin Multiplatform module: the main Room database, the Ktor API client, calculators, Arcaea domain constants, and the Compose theme
and components `:desktopApp` also uses.

Sources live under `src/<sourceSet>/kotlin/`.

## Layout (commonMain)

```
xyz.sevive.arcaeaoffline/
├── core/
│   ├── api/          Ktor client: ArcaeaResourcesApiClient, DownloadableResource,
│   │                 RemoteResourcesInfoStateHolder
│   ├── calculators/  pure math (potential/B30); keep dependency-free and unit-tested
│   ├── constants/    Arcaea domain enums/constants (rating classes, clear types, ...)
│   └── database/     the main Room database (largest package; below)
└── ui/
    ├── theme/        expect Theme + CustomColors, ColorSchemes, Typography, Spacing
    ├── components/   cross-platform composables (e.g. ArcaeaScoreTextField)
    └── utils/        UI helpers (e.g. AutoRepeatController)
```

## core/database (Room 3)

`ArcaeaOfflineDatabase` is the main database. Package-by-layer inside:

- `entities/` — bare domain names (`Song.kt`, `PlayResult.kt`), including localized variants.
- `daos/` — `XxxDao.kt`, returning `Flow` for queries.
- `repositories/` — an interface plus its `*Impl` in the same file; consumers depend on the interface.
- `migrations/` — `Migration_X_Y` and `AutoMigration_X_Y`. `.editorconfig` relaxes class naming and line length for these files; do not
  reformat migration SQL.
- `externals/` — importers and exporters for external game formats (songlist, st3 saves, DEFv2) via `kotlinx.serialization`.
- `converters/`, `extensions/`, `r30/`.

Schema JSONs export to `shared/schemas/`. Bumping a version means raising `version` in the `@Database` annotation, writing the migration,
and committing the exported JSON in the same change. Always prefer `AutoMigration` when possible.

## Resources & build config

- Compose Multiplatform resources live in `src/commonMain/composeResources/`, exposed as package
  `xyz.sevive.arcaeaoffline.resources`.
- `BuildKonfig` is generated from `local.properties`, falling back to `local.defaults.properties`.

## Tests

- `commonTest` runs on the host JVM and, via `withDeviceTestBuilder` (source-set tree `test`), on devices too. Tests the Android host runner
  cannot run are annotated `@Category(AndroidHostTestIncompatible::class)`
  (`src/commonTest/kotlin/xyz/sevive/arcaeaoffline/test/category/AndroidHostTestIncompatible.kt`) and excluded from `testAndroidHostTest`.
- `jvmTest` hosts the Room migration tests, DAO tests, and importer tests with fixtures under `src/jvmTest/resources/`. Its working
  directory is `shared/`, so relative fixture paths resolve from there.
