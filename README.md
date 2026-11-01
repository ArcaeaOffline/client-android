# Arcaea Offline Android Client

[![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/ArcaeaOffline/client-android)

> [!WARNING]
> This project is **mostly AI-generated** with **light review**.
>
> If you'd like to contribute, feel free to use AI assistance.
> Consider saving your precious time and manual effort for higher-quality, human-crafted projects.

## Custom Maven Repo (Optional)

To reduce APK size, this project builds custom ONNX Runtime and OpenCV in [ArcaeaOffline/custom-lib-builds](https://github.com/ArcaeaOffline/custom-lib-builds), with unused features removed.

`settings.gradle.kts` gives the local maven repo the highest priority, simply download the matching artifacts from the custom-lib-builds Releases and unpack them.

If the local maven repo does not exist, Gradle falls back to Maven Central. This only affects APK size, not functionality.

## Documentation

- [ARCHITECTURE.md](ARCHITECTURE.md): module boundaries and where new code goes
- [potential.md](./docs/potential.md) (Chinese): potential calculation
- [st3.md](./docs/st3.md) (Chinese): caveats of the in-game score database

## License

[GPL-3.0](LICENSE)
