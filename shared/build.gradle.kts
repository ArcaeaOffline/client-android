plugins {
//    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.multiplatform)
//    kotlin("multiplatform")
}

kotlin {
//    androidTarget()
    jvm()

    sourceSets {
        jvmMain.dependencies { }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
