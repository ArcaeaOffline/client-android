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
}

rootProject.name = "Arcaea Offline"
include("app")
include("opencv")
