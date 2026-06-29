plugins {
    kotlin("jvm")
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.compose.compiler)
}

dependencies {
    implementation(project(":shared"))
    implementation(compose.desktop.currentOs)

    implementation(libs.compose.runtime)
    implementation(libs.compose.material3)
    implementation(libs.compose.ui)
    implementation(libs.compose.foundation)
}

compose.desktop {
    application {
        mainClass = "xyz.sevive.arcaeaoffline.desktop.MainKt"
    }
}
