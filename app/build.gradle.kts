import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.ksp)
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.plugin.serialization)
    alias(libs.plugins.secrets.gradle.plugin)
    alias(libs.plugins.protobuf)
    alias(libs.plugins.androidGitVersion)
    alias(libs.plugins.aboutlibraries)
    alias(libs.plugins.compose.compiler)
    alias(androidx.plugins.room)
}

secrets {
    defaultPropertiesFileName = "local.defaults.properties"
    ignoreList += listOf(
        "SENTRY_DSN",
        "SIGNING_STORE_PASSWORD",
        "SIGNING_KEY_ALIAS",
        "SIGNING_KEY_PASSWORD",
    )
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:4.27.1"
    }

    // Generates the java Protobuf-lite code for the Protobufs in this project. See
    // https://github.com/google/protobuf-gradle-plugin#customizing-protobuf-compilation
    // for more information.
    generateProtoTasks {
        all().forEach { task ->
            task.plugins {
                create("java") {
                    option("lite")
                }
            }
        }
    }
}


// https://stackoverflow.com/a/69268957/16484891, CC BY-SA 4.0
val localProperties = Properties().apply {
    load(FileInputStream(File(rootDir, "local.properties")))
}

room {
    schemaDirectory("$projectDir/schemas")
}

android {
    namespace = "xyz.sevive.arcaeaoffline"
    compileSdk = 36

    defaultConfig {
        applicationId = "xyz.sevive.arcaeaoffline"
        minSdk = 21
        targetSdk = 34
        versionCode = androidGitVersion.code()
        versionName = androidGitVersion.name()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        manifestPlaceholders["sentryDsn"] = localProperties.getProperty("SENTRY_DSN")
    }

    signingConfigs {
        create("release") {
            storeFile = File(rootDir, "arcaea_offline.jks")
            storePassword = localProperties.getProperty("SIGNING_STORE_PASSWORD")
            keyAlias = localProperties.getProperty("SIGNING_KEY_ALIAS")
            keyPassword = localProperties.getProperty("SIGNING_KEY_PASSWORD")

            enableV1Signing = true
            enableV2Signing = true
            enableV3Signing = true
            enableV4Signing = true
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
        }

        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    flavorDimensions += "app_stable"

    productFlavors {
        create("stable") {
            dimension = "app_stable"
        }
        create("unstable") {
            dimension = "app_stable"
            applicationIdSuffix = ".unstable"
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
    // android & androidx
    api(androidx.room.runtime)
    ksp(androidx.room.compiler)
    implementation(androidx.room.ktx)

    implementation(androidx.datastore.core)
    implementation(androidx.datastore.preferences)

    implementation(androidx.core.ktx)
    implementation(androidx.activity.compose)

    val composeBom = platform(androidx.compose.bom)
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation(androidx.compose.ui)
    implementation(androidx.compose.ui.graphics)
    implementation(androidx.compose.ui.tooling.preview)
    debugImplementation(androidx.compose.ui.tooling)
    implementation(androidx.compose.material.icons.extended)

    implementation(androidx.compose.material3)
    implementation(androidx.compose.material3.window.size.cls)
    implementation(androidx.compose.material3.adaptive)
    implementation(androidx.compose.material3.adaptive.navigation.suite)
    implementation(androidx.compose.material3.adaptive.layout)
    implementation(androidx.compose.material3.adaptive.navigation)

    implementation(androidx.lifecycle.lifecycleViewmodelCompose)
    implementation(androidx.lifecycle.lifecycleRuntimeCompose)

    implementation(androidx.exifinterface)

    implementation(androidx.navigation.compose)

    implementation(androidx.documentfile)

    implementation(androidx.work.workRuntime)
    implementation(androidx.work.workRuntimeKtx)

    // 3rd party
    implementation(libs.kotlinx.serialization)

    implementation(libs.io.sentry.sentryAndroid)

    implementation(libs.opencv)

    implementation(libs.onnxruntime.android)

    implementation(libs.apache.commons.io)

    implementation(libs.protobuf.protobufJavalite)

    implementation(libs.github.requery.sqlite.android)

    implementation(libs.threetenabp)

    implementation(libs.compose.richtext.commonmark)
    implementation(libs.compose.richtext.ui.material3)

    implementation(libs.github.jvziyaoyao.scale.imageViewer)

    implementation(libs.github.cheonjaeung.gridlayout)

    implementation(libs.aboutlibrariesCore)
    implementation(libs.aboutlibrariesComposeM3)

    // test & debug
    testImplementation(libs.junit)
    androidTestImplementation(androidx.test.ext.junit)
    androidTestImplementation(androidx.test.espresso.core)
    androidTestImplementation(androidx.compose.ui.test.junit4)

    debugImplementation(androidx.compose.ui.test.manifest)

    implementation(project(":core"))
}
