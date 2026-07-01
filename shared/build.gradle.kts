import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.kotlin.multiplatform)

    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.jetbrains.compose)

    alias(libs.plugins.kotlin.plugin.serialization)
}

compose {
    resources {
        publicResClass = true
        packageOfResClass = "xyz.sevive.arcaeaoffline.resources"
        generateResClass = always
    }
}

tasks.withType<Test>().configureEach {
    if (name == "testAndroidHostTest") {
        useJUnit {
            excludeCategories("xyz.sevive.arcaeaoffline.test.category.AndroidHostTestIncompatible")
        }
    }
}

kotlin {
    jvm()

    android {
        namespace = "xyz.sevive.arcaeaoffline.shared"
        compileSdk = 37
        minSdk = 24

        withJava()
        withHostTest {
            isIncludeAndroidResources = true
        }
        withDeviceTestBuilder {
            // Link Android device tests to the KMP 'test' source set tree
            // so commonTest (including UI tests) will also run on the devices.
            sourceSetTreeName = "test"
        }

        androidResources {
            enable = true
        }

        compilerOptions {
            jvmTarget = JvmTarget.JVM_17
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.ui.graphics)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material.icons.extended)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.ui.tooling)

            implementation(libs.kotlinx.serialization)

            implementation(libs.bignum)
        }

        jvmMain.dependencies {
            implementation(libs.jSystemThemeDetector)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.compose.ui.test)
        }

        jvmTest.dependencies {
            implementation(compose.desktop.currentOs)
        }
    }
}
