plugins {
//    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.jetbrains.compose)
}

compose {
    resources {
        publicResClass = true
        packageOfResClass = "xyz.sevive.arcaeaoffline.resources"
        generateResClass = always
    }
}

kotlin {
//    androidTarget()
    jvm()

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

            implementation(libs.bignum)
        }

        jvmMain.dependencies { }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
