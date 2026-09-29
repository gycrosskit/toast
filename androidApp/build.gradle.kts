plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "io.github.gycrosskit.toast.sample"
    compileSdk = 36

    defaultConfig {
        applicationId = "io.github.gycrosskit.toast.sample"
        minSdk = 24
        targetSdk = 36
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(project(":toast"))
}
