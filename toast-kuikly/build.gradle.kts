plugins {
    alias(libs.plugins.kotlin.multiplatform)
    `maven-publish`
}

kotlin {
    ohosArm64()
    sourceSets {
        commonMain.dependencies {
            api(project(":toast"))
            implementation(libs.kuikly.core)
        }
    }
}
