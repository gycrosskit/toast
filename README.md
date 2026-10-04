# GY CrossKit Toast

Android、iOS 和 HarmonyOS 的短消息展示。新消息替换旧消息；文案和本地化由宿主提供。KMP 入口为 `MessagePlatform`，CMP 提供 CompositionLocal，HarmonyOS Kuikly 提供 Module。

本轮 Maven/Swift/Git Pod/HAR 候选为 0.1.3，[prerelease 已发布](https://github.com/gycrosskit/toast/releases/tag/0.1.3)，实际下载 SHA 与 JitPack 全 13 个 module 的文件引用校验通过。独立真实 JitPack OHOS Kuikly、Android/iOS CMP 编译及 Simulator 最终链接、Release HAR 独立编译通过；安装示例使用候选精确版本，设备展示尚未验收。

OHPM `next` 提交已接受，仍在审核；精确版本查询及独立 Registry 安装返回 NOTFOUND。稳定 Registry `latest` 仍为 0.1.2，Release HAR 可下载不代表 Registry 可安装。

## 平台与要求

| 平台 | 接入方式 | 系统要求 |
| --- | --- | --- |
| Android | `toast`，可选 `toast-cmp` | API 24+ |
| iOS | KMP bridge + `GycToastNative`，或直接 Swift Package/CocoaPods | iOS 15+，Swift tools 5.9 |
| HarmonyOS | `toast-kuikly` + `toast-native` HAR，或直接 ArkTS | 当前 HAR 的 target/compatible SDK 均为 API 22；`openToast/closeToast` API 本身从 API 18 提供 |

KMP 使用 Kotlin `2.2.21-1.0.0`，CMP 使用 Compose `1.10.3`，Kuikly 使用 `2.28.0-2.0.21-ohos`。`toast-core/` 的 Gradle 模块名及公开 Maven artifact 为 `toast`。

## 安装

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        maven("https://jitpack.io")
        maven("https://maven.eazytec-cloud.com/nexus/repository/maven-public/")
        google()
        mavenCentral()
    }
}
```

```kotlin
commonMain.dependencies {
    implementation("com.github.gycrosskit.toast:toast:0.1.3")
    // Compose Multiplatform 宿主额外添加：
    implementation("com.github.gycrosskit.toast:toast-cmp:0.1.3")
}
// HarmonyOS Kuikly 宿主额外添加：
ohosArm64Main.dependencies {
    implementation("com.github.gycrosskit.toast:toast-kuikly:0.1.3")
}
```

iOS 在 Xcode 的 Package Dependencies 添加 `https://github.com/gycrosskit/toast.git`，选择精确版本 `0.1.3`，产品 `GycToastNative`。CocoaPods 可按 Git tag 安装，见接入指南。

HarmonyOS 原生包独立安装，候选正式可查询后执行；发布接受与 Registry 可安装分别核验：

```sh
ohpm install @gycrosskit/toast-native@0.1.3
```

旧稳定 `0.1.2` 有 Git tag、JitPack Maven 和 OHPM 包，未补写旧 Release。新候选 `0.1.3` 的 Swift Package 和 Git Pod 已分别完成独立真实原生消费：公开 API 编译及动态消费者/framework 最终链接均通过，产物为 arm64/x86_64 iOS Simulator Mach-O。SPM 锁定发布提交，Git Pod framework 版本为 0.1.3；这两个渠道单独验收，不以 KMP framework 链接代替。

## 最小使用

```kotlin
import io.github.gycrosskit.toast.*
import io.github.gycrosskit.toast.cmp.*

// Android：在应用范围复用同一个实例。
val messages = AndroidMessagePlatform.get(applicationContext)
messages.show("操作完成")
// 在 Compose 应用根部：
ProvideMessagePlatform(messages) {
    // 页面内可调用 rememberMessages().show("操作完成")。
}
```

```swift
import GycToastNative
// 稳定的根 UIViewController 创建后绑定：
GycToastPresenter.shared.bind(rootController: rootController)
GycToastPresenter.shared.show(message: "操作完成")
```

## 生命周期与边界

Android 的不同 UI 引擎复用 `AndroidMessagePlatform.get(applicationContext)`，避免各自展示竞争。iOS 使用不可点击的独立 UIWindow，需要有效根控制器；KMP 宿主通过 `IosMessageBridge` 转发到原生 presenter。HarmonyOS 需要已创建主窗口的 UIAbilityContext 或注册 `GycToastModule`。

无需额外系统权限。空白文案不展示；仅支持短/长时长和新消息替换，不提供消息队列或交互式提示。原生窗口显示、替换和多窗口行为仍需宿主设备验收。

## 文档与帮助

- [接入指南](docs/接入指南.md)：平台初始化、权限声明和生命周期。
- [开发与验证](docs/开发与验证.md)：源码构建、检查命令与验收范围。
- [版本与发行说明](https://github.com/gycrosskit/toast/releases)、[问题反馈](https://github.com/gycrosskit/toast/issues)。

Apache-2.0，见 [LICENSE](LICENSE)。

## 0.1.3 候选：Kuikly 页面生命周期

每个 Pager 注册组件 `ToastModule` 和原生 `GycToastModule`，业务只映射消息文案/时长；`pageWillDestroy` 调用 `ToastModule.dispose()`，原生 `onDestroy` 后拒绝消息。
不再把showMessage协议复制到应用的系统动作Module；已显示Toast仍由唯一presenter管理，不由旧Page销毁新Page消息。

候选 Maven 的 core/CMP/Kuikly 坐标均显式选择 0.1.3，HAR 为 `@gycrosskit/toast-native@0.1.3`。Maven 已完成远程文件校验，OHPM 可安装性按上方独立状态记录。
