pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    @Suppress("UnstableApiUsage")
    repositories {
        google()
        mavenCentral()

        maven {
            // used by
            // 'com.github.requery:sqlite-android'
            setUrl("https://jitpack.io")
        }
    }

    versionCatalogs {
        create("androidx") {
            from(files("gradle/androidx.versions.toml"))
        }
    }
}

rootProject.name = "Arcaea Offline"
include("app")
include(":core")
include(":shared")
include(":desktopApp")
