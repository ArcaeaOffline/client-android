pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
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
