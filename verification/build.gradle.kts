plugins {
    kotlin("multiplatform") version "2.2.21-1.0.0"
    kotlin("plugin.compose") version "2.2.21-1.0.0" apply false
    id("com.android.library") version "8.10.1"
}
val toastVersion = providers.gradleProperty("toastVersion").orElse("0.1.3").get()
// CMP 的编译插件只用于 Android/iOS 探针，OHOS Kuikly 不依赖 Compose Runtime。
val verifyCmp = providers.gradleProperty("verifyCmp").orElse("false").get().toBoolean()
val verifyUnifiedUi = providers.gradleProperty("verifyUnifiedUi").orElse("false").get().toBoolean()
if (verifyCmp || verifyUnifiedUi) apply(plugin = "org.jetbrains.kotlin.plugin.compose")
kotlin {
    androidTarget()
    iosArm64()
    iosX64 { binaries.framework { baseName = "ToastConsumer" } }
    iosSimulatorArm64 { binaries.framework { baseName = "ToastConsumer" } }
    ohosArm64()
    sourceSets {
        commonMain {
            if (!verifyUnifiedUi) kotlin.exclude("**/Unified*.kt")
            dependencies {
                implementation("com.github.gycrosskit.toast:toast:$toastVersion")
                if (verifyUnifiedUi) implementation("com.github.gycrosskit.toast:toast-kuikly:$toastVersion")
            }
        }
        androidMain { if (!verifyUnifiedUi) kotlin.exclude("**/Unified*.kt") }
        iosMain { if (!verifyUnifiedUi) kotlin.exclude("**/Unified*.kt") }
        androidMain.dependencies {
            implementation("com.github.gycrosskit.toast:toast-cmp:$toastVersion")
            if (!verifyUnifiedUi) implementation("org.jetbrains.compose.runtime:runtime:1.10.3")
        }
        iosMain.dependencies {
            implementation("com.github.gycrosskit.toast:toast-cmp:$toastVersion")
            if (!verifyUnifiedUi) implementation("org.jetbrains.compose.runtime:runtime:1.10.3")
        }
        ohosArm64Main.dependencies { implementation("com.github.gycrosskit.toast:toast-kuikly:$toastVersion") }
    }
}
android { namespace = "io.github.gycrosskit.toast.consumer"; compileSdk = 36; defaultConfig { minSdk = 24 } }
