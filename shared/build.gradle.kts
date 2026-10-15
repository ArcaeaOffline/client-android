import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.ksp)

    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.jetbrains.compose)

    alias(libs.plugins.kotlin.plugin.serialization)

    alias(androidx.plugins.room3)
}

room3 {
    schemaDirectory("$projectDir/schemas")
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
    compilerOptions {
        // Room3 compiler opt out
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

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
            implementation(libs.kotlinx.datetime)

            implementation(libs.bignum)
            implementation(libs.kermit)

            implementation(libs.ktor.client.core)

            api(androidx.room3.runtime)
            implementation(androidx.sqlite.bundled)
        }

        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
        }

        jvmMain.dependencies {
            // jSystemThemeDetector pulls in jfa, whose POM pins jna with a non-existent
            // "jpms" classfier, breaking jvm configuration with "jna-<version>-jpms.jar"
            // not found.
            //
            // filekit-core depends on jna-platform which introduces plain jna, so excluding
            // this does not affect compilation.
            implementation("com.github.Dansoftowner:jSystemThemeDetector:${libs.versions.jSystemThemeDetector.get()}") {
                exclude(group = "net.java.dev.jna", module = "jna")
            }
            implementation(libs.ktor.client.cio)
            implementation(libs.filekit.core)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.compose.ui.test)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.mock)
            implementation(libs.kotlinx.coroutines.test)
        }

        jvmTest.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(androidx.room3.testing)
        }
    }
}

dependencies {
    add("kspAndroid", androidx.room3.compiler)
    add("kspJvm", androidx.room3.compiler)
}
