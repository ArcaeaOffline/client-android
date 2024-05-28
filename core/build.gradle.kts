plugins {
    alias(libs.plugins.ksp)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.plugin.serialization)

    alias(androidx.plugins.room)
}

android {
    namespace = "xyz.sevive.arcaeaoffline.core"
    compileSdk = 34

    defaultConfig {
        minSdk = 21

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")

        room {
            schemaDirectory("$projectDir/schemas")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation(libs.kotlinx.serialization)

    // android & androidx
    api(androidx.room.runtime)
    ksp(androidx.room.compiler)
    implementation(androidx.room.ktx)

    // 3rd party
    implementation(libs.apache.commons.io)
    implementation(libs.github.requery.sqlite.android)
    implementation(libs.threetenabp)

    // test & debug
    testImplementation(libs.junit)
    androidTestImplementation(libs.junit)
    androidTestImplementation(androidx.test.ext.junit)
}
