plugins {
    kotlin("multiplatform") version "2.2.21-1.0.0"
    kotlin("plugin.compose") version "2.2.21-1.0.0"
    id("com.android.library") version "8.10.1"
}
val toastVersion = providers.gradleProperty("toastVersion").orElse("0.2.0-rc.1").get()
val verifyLocalSource = providers.gradleProperty("verifyLocalSource").isPresent
check(!verifyLocalSource || !providers.gradleProperty("remoteOnly").isPresent) { "Local source and remote-only validation are separate modes" }
val kuiklyRenderFrameworkDir = providers.gradleProperty("kuiklyRenderFrameworkDir").orNull
kotlin {
    androidTarget()
    iosArm64()
    iosX64 { binaries.framework {
        baseName = "ToastConsumer"
        kuiklyRenderFrameworkDir?.let { linkerOpts("-F$it", "-framework", "OpenKuiklyIOSRender") }
    } }
    iosSimulatorArm64 { binaries.framework {
        baseName = "ToastConsumer"
        kuiklyRenderFrameworkDir?.let { linkerOpts("-F$it", "-framework", "OpenKuiklyIOSRender") }
    } }
    ohosArm64()
    sourceSets {
        commonMain.dependencies {
            implementation("com.github.gycrosskit.toast:toast:$toastVersion")
            implementation("com.github.gycrosskit.toast:toast-kuikly:$toastVersion")
        }
    }
}
android { namespace = "io.github.gycrosskit.toast.consumer"; compileSdk = 36; defaultConfig { minSdk = 24 } }

tasks.register("verifyNoCmpUi") {
    doLast {
        val resolution = configurations.getByName("debugRuntimeClasspath").incoming.resolutionResult
        val unresolved = resolution.allDependencies.filterIsInstance<org.gradle.api.artifacts.result.UnresolvedDependencyResult>()
        check(unresolved.isEmpty()) { "Incomplete runtime dependency graph: ${unresolved.map { it.attempted }}" }
        val components = resolution.allComponents
        val deps = components.mapNotNull { it.moduleVersion }
        val forbidden = deps.filter {
            it.name.startsWith("toast-cmp") ||
                listOf("org.jetbrains.compose", "androidx.compose").any { group ->
                    listOf("ui", "foundation", "material").any { ui -> it.group.startsWith("$group.$ui") }
                }
        }
        check(forbidden.isEmpty()) { "Kuikly consumer pulls a second CMP UI: $forbidden" }
        check(deps.any { it.group == "com.tencent.kuikly-open" && it.name.startsWith("compose") })
        val toast = if (verifyLocalSource) {
            components.filter { it.id is org.gradle.api.artifacts.component.ProjectComponentIdentifier }
                .mapNotNull { it.moduleVersion }.filter { it.name in setOf("toast", "toast-kuikly") }
                .also { check(it.map { module -> module.name }.toSet() == setOf("toast", "toast-kuikly")) }
        } else deps.filter { it.group == "com.github.gycrosskit.toast" }
        check(toast.isNotEmpty() && toast.all { it.version == toastVersion }) { "Unexpected Toast versions: $toast" }
        println("PASS KuiklyCompose present; no second CMP UI; exact Toast version $toastVersion; local source=$verifyLocalSource")
    }
}
