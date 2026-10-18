pluginManagement {
    repositories {
        maven("https://maven.eazytec-cloud.com/nexus/repository/maven-public/")
        maven("https://maven.aliyun.com/repository/google")
        maven("https://maven.aliyun.com/repository/public")
        google(); mavenCentral(); gradlePluginPortal()
        maven("https://mirrors.tencent.com/nexus/repository/maven-public/")
    }
}
dependencyResolutionManagement {
    repositories {
        maven {
            url = uri(providers.gradleProperty("toastMavenRepo").orElse("https://jitpack.io").get())
            content { includeGroup("com.github.gycrosskit.toast") }
        }
        maven("https://maven.eazytec-cloud.com/nexus/repository/maven-public/")
        maven("https://maven.aliyun.com/repository/google")
        maven("https://maven.aliyun.com/repository/public")
        google(); mavenCentral()
        maven("https://mirrors.tencent.com/nexus/repository/maven-public/")
        maven("https://mirrors.tencent.com/nexus/repository/maven-tencent/")
    }
}
rootProject.name = "toast-consumer"

// 显式本地源码消费仅用于未发布候选，不能证明远程 Maven 可用。
if (providers.gradleProperty("verifyLocalSource").isPresent) {
    includeBuild("..") {
        dependencySubstitution {
            substitute(module("com.github.gycrosskit.toast:toast")).using(project(":toast"))
            substitute(module("com.github.gycrosskit.toast:toast-kuikly")).using(project(":toast-kuikly"))
        }
    }
}
