# AGENTS.md

Before adding, moving, or renaming a package or file, read [ARCHITECTURE.md](ARCHITECTURE.md): it covers the module boundaries and where new
code goes.

## Layered AGENTS.md

Module-level instructions live next to the module:

- [`app/AGENTS.md`](app/AGENTS.md)
- [`shared/AGENTS.md`](shared/AGENTS.md)
- [`core/AGENTS.md`](core/AGENTS.md)

Before editing files under `app/`, `shared/`, or `core/`, read the nearest of these. It overrides this file where they conflict.
`desktopApp/` has no separate file and is covered by this one.

## Build & checks

Run Gradle with `./gradlew` (Git Bash / Linux) or `gradlew.bat` (Windows cmd).

| Task                                        | Purpose                                          |
|---------------------------------------------|--------------------------------------------------|
| `./gradlew check`                           | Full CI gate (`.github/workflows/check.yml`)     |
| `:app:compileUnstableDebugKotlin`           | Compile the app; flavor is part of the task name |
| `:app:lintUnstableDebug`, `:core:lintDebug` | Android Lint                                     |
| `lintKotlin`, `formatKotlin`                | ktlint via kotlinter                             |
| `:shared:jvmTest`                           | Shared-module unit tests on the host JVM         |

`connectedDebugAndroidTest` needs a device or emulator and the OCR model binaries. Check `.github/actions/setup-ocr-model/action.yml` for
setup instructions if ocr assets do not exist.

## Conventions

- **Style**: ktlint with the kotlinter Gradle plugin. Project preferences live in [.editorconfig](.editorconfig). Run `formatKotlin` on
  touched modules before declaring done.
- **Kotlin sources**:
    - `:app` uses `src/main/java/`
    - `:core` and `:desktopApp` use `src/main/kotlin/`
    - `:shared` uses `src/<sourceSet>/kotlin/`
- **DI**: Koin with the compiler-plugin module DSL and explicit registrations. See [`app/AGENTS.md`](app/AGENTS.md).

## Editing these instructions

When a code change invalidates a rule, update the doc in the same change.
