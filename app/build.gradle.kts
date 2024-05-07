import java.io.ByteArrayOutputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.devtools.ksp")
    alias(libs.plugins.secrets.gradle.plugin)

    alias(libs.plugins.kotlin.plugin.serialization)
}

secrets {
    defaultPropertiesFileName = "local.defaults.properties"
}

// automatic version generating from https://stackoverflow.com/a/24121734/16484891
// CC BY-SA 3.0
fun getVersionCode(): Int {
    return try {
        val stdout = ByteArrayOutputStream()
        exec {
            commandLine("git", "rev-list", "--first-parent", "--count", "master")
            standardOutput = stdout
        }
        Integer.parseInt(stdout.toString().trim())
    } catch (ignored: Exception) {
        -1
    }
}

fun getVersionName(): String? {
    return try {
        val stdoutStream = ByteArrayOutputStream()
        exec {
            commandLine("git", "describe", "--tags", "--dirty")
            standardOutput = stdoutStream
        }
        val stdout = stdoutStream.toString().trim()
        stdout
    } catch (ignored: Exception) {
        null
    }
}

android {
    namespace = "xyz.sevive.arcaeaoffline"
    compileSdk = 34

    defaultConfig {
        applicationId = "xyz.sevive.arcaeaoffline"
        minSdk = 21
        targetSdk = 34
        versionCode = getVersionCode()
        versionName = getVersionName()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    flavorDimensions.add("app_stable")

    productFlavors {
        create("stable") {
            dimension = "app_stable"
        }
        create("unstable") {
            dimension = "app_stable"
            applicationIdSuffix = ".unstable"
            versionNameSuffix = "-unstable"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        buildConfig = true
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.4"
    }
    packaging {
        resources.excludes.add("/META-INF/{AL2.0,LGPL2.1}")
    }
    splits {
        abi {
            isEnable = true
            reset()
            include("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
            isUniversalApk = true
        }
    }
}

dependencies {
    implementation(libs.kotlinx.serialization)

    // android & androidx
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    implementation(libs.androidx.datastore)
    implementation(libs.androidx.datastore.preferences)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)

    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation(libs.androidx.compose.material.icons.extended)

    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material3.window.size.cls)
    implementation(libs.androidx.compose.material3.adaptive)

    implementation(libs.androidx.window)

    implementation(libs.androidx.appcompact)

    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    implementation(libs.androidx.exifinterface)

    implementation(libs.androidx.navigation.compose)

    implementation(libs.android.material)

    // 3rd party
    implementation(libs.apache.commons.io)

    implementation(libs.github.requery.sqlite.android)

    implementation(libs.acra.http)
    implementation(libs.acra.dialog)

    implementation(libs.threetenabp)

    implementation(libs.compose.richtext.commonmark)
    implementation(libs.compose.richtext.ui.material3)

    implementation(libs.github.jvziyaoyao.imageviewer)

    implementation(libs.github.cheonjaeung.gridlayout)

    // test & debug
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)

    debugImplementation(libs.androidx.compose.ui.test.manifest)

    implementation(project(":opencv"))
}
