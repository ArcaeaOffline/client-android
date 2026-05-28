import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.ksp)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.plugin.serialization)

    alias(androidx.plugins.room)
}

room {
    schemaDirectory("$projectDir/schemas")
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_1_8
    }
}

android {
    namespace = "xyz.sevive.arcaeaoffline.core"
    compileSdk = 36

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    sourceSets {
        // Adds exported schema location as test app assets.
        getByName("androidTest").assets.directories += setOf("$projectDir/schemas")
    }
}

dependencies {
    implementation(libs.kotlinx.serialization)

    // android & androidx
    implementation(androidx.core.ktx)

    api(androidx.room.runtime)
    ksp(androidx.room.compiler)
    implementation(androidx.room.ktx)
    implementation(androidx.sqlite.bundled)

    // 3rd party
    implementation(libs.kermit)

    implementation(libs.okio)
    implementation(libs.apache.commons.io)
    implementation(libs.threetenabp)

    implementation(libs.opencv)
    implementation(libs.onnxruntime.android)

    // test & debug
    testImplementation(libs.junit)
    androidTestImplementation(libs.junit)
    androidTestImplementation(androidx.test.ext.junit)
    testImplementation(androidx.room.testing)
    androidTestImplementation(androidx.test.runner)
    androidTestImplementation(androidx.room.testing)
}
