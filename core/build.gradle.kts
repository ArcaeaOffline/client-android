import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.ksp)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.plugin.serialization)
    alias(libs.plugins.koin.compiler)
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
        freeCompilerArgs.add("-Xannotation-default-target=param-property")
    }
}

android {
    namespace = "xyz.sevive.arcaeaoffline.core"
    compileSdk = 37

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
        isCoreLibraryDesugaringEnabled = true

        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(project(":shared"))

    implementation(libs.kotlinx.io)
    implementation(libs.kotlinx.serialization)
    implementation(libs.ktoml.core)
    implementation(libs.kotlinx.datetime)
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.core)
    implementation(libs.koin.annotations)
    implementation(libs.koin.android)

    // android & androidx
    coreLibraryDesugaring(libs.desugarJdkLibs)

    implementation(androidx.core.ktx)

    // 3rd party
    implementation(libs.kermit)

    implementation(libs.opencv)
    implementation(libs.onnxruntime.android)

    // test & debug
    testImplementation(libs.junit)
    androidTestImplementation(libs.junit)
    androidTestImplementation(androidx.test.ext.junit)
    androidTestImplementation(androidx.test.runner)
}
