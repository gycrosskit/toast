plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.compiler)
    `maven-publish`
}

kotlin {
    androidTarget {
        publishLibraryVariants("release")
        compilerOptions { jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11) }
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    ohosArm64()
    sourceSets {
        androidMain.dependencies { api("com.tencent.kuikly-open:core-render-android:${libs.versions.kuikly.get()}") }
        commonTest.dependencies { implementation(kotlin("test")) }
        androidUnitTest.dependencies { implementation("org.robolectric:robolectric:4.16.1") }
        commonMain.dependencies {
            api(project(":toast"))
            api(libs.kuikly.core)
            api(libs.kuikly.compose)
        }
    }
}

android {
    namespace = "io.github.gycrosskit.toast.kuikly"
    compileSdk = 36
    defaultConfig { minSdk = 24 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
