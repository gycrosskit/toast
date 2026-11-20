# GY CrossKit Toast

新版 Maven 发布由 Jenkins 生成同版本归档、`source-receipt.json` 和 `SHA256SUMS`。JitPack 核对标签来源、归档尺寸和摘要后安装同一份制品。历史发布按原版本 Tag 复验。

Kuikly 由宿主统一选择版本；组件构建基线、允许范围及消费验证边界见[功能与平台差异](docs/功能与平台差异.md#kuikly-版本与宿主选择)。

当前已发布 Maven 为 [`1.0.1`](https://github.com/gycrosskit/toast/releases/tag/1.0.1)，Git Pod `native-1.0.2` 与私有 OHPM `1.0.2` 已发布。本轮只准备 Maven `1.0.2` 候选，尚未发布。Maven Tag 同时提供 SPM presenter 源码，SPM 与 Maven 消费仍分别验收。

Harmony 后续仅使用受信私 OHPM。当前私库元数据已有 `@gycrosskit/toast-native@1.0.2`；历史 HAR Release 保留，私库元数据可查询不等于安装或宿主消费已通过。本地构建继续调用项目入口，日常 CI 不串联测试或独立消费者。正式发布统一使用新版 `component-ci/release`，本地 package 不创建 Tag 或上传。

当前只维护 `toast` core 与 `toast-kuikly`，已移除 `toast-cmp`。这是删除 CMP 公开坐标/API 的不兼容变更；旧 CMP 使用方应继续锁定已发布的 `0.1.x`，或迁移至 KuiklyCompose/native 入口，不能直接升级。

KuiklyCompose Android/iOS/OHOS 的 `MessageHost`、CompositionLocal、Compose compiler/runtime，以及共享 Overlay 状态、规格、定时替换和销毁合同保留。Android/iOS/OHOS 原生提示与 Module 也保留；文案和本地化由宿主提供，legacy 原生提示与 Overlay 的视觉/时长合同不同。详见[功能与平台差异](docs/功能与平台差异.md)。

历史 0.1.x / RC 的发布、消费与 Registry 记录见[候选验收](verification/候选验收.md)和[RC 迁移说明](docs/RC迁移说明.md)，不代算本轮精确公开消费或设备验收。当前 Registry 可安装性须独立核验，Release HAR 下载不等于 Registry 安装。

## 平台与要求

| 平台 | 接入方式 | 系统要求 |
| --- | --- | --- |
| Android | Overlay用`toast-kuikly`；legacy用`toast` | API 24+ |
| iOS | Overlay用`toast-kuikly`；legacy用KMP bridge+`GycToastNative`或Swift/Pod | iOS 15+，Swift tools 5.9（原生包） |
| HarmonyOS | Overlay用`toast-kuikly`；legacy用Module+`toast-native` HAR或ArkTS | 当前 HAR 的 target/compatible SDK 均为 API 22；`openToast/closeToast` API 本身从 API 18 提供 |

KMP 使用 Kotlin `2.2.21-1.0.0`，Kuikly 使用 `2.29.1-2.0.21-ohos`。`toast-core/` 的 Gradle 模块名及公开 Maven artifact 为 `toast`。

## 架构与调用流程

`MessageHost` 渲染页面独占的 `MessageOverlayState`，并通过 `rememberMessages()` 提供同一个 `MessagePlatform`。Android、iOS 和 HarmonyOS 各自复用原生 presenter。HarmonyOS 通过 `ToastModule` 异步转发，已显示的提示继续归共享 presenter 管理。接线和时长见[功能与平台差异](docs/功能与平台差异.md)，源码入口见 [AOCI 索引](aoci.txt)。

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

以下为 Maven `1.0.2` 未发布候选坐标；发布前继续使用 `1.0.1`，Release 归档、JitPack 解析与精确消费须分别核验：

```kotlin
commonMain.dependencies {
    implementation("com.github.gycrosskit.toast:toast-kuikly:1.0.2")
    // 仅使用 native core 时，改为依赖 toast:1.0.2。
}
```

iOS 在 Xcode 的 Package Dependencies 添加 `https://github.com/gycrosskit/toast.git`，候选发布后选择精确 Tag `1.0.2`，产品 `GycToastNative`；SPM 消费须独立核验。CocoaPods 使用独立的 `native-1.0.2` Tag，见接入指南。

HarmonyOS 通过私有 OHPM 安装精确版本 `1.0.2`；以对应 Registry 查询与发布回执为准：

```sh
ohpm install @gycrosskit/toast-native@1.0.2
```

旧稳定 `0.1.2` 有 Git tag、JitPack Maven 和 OHPM 包，未补写旧 Release。历史 `1.0.0` 的 Swift Package 和 Git Pod 已分别完成独立真实原生消费：公开 API 编译及动态消费者/framework 最终链接均通过，产物为 arm64/x86_64 iOS Simulator Mach-O。SPM 锁定发布提交，Git Pod framework 版本为 1.0.0；这两个渠道单独验收，不以 KMP framework 链接代替。 该历史结果不代表本轮 `1.0.2` 已通过。

## 最小使用

```kotlin
import androidx.compose.runtime.remember
import io.github.gycrosskit.toast.*
import io.github.gycrosskit.toast.kuikly.*

// 在 KuiklyCompose 页面根部记住一个 state；移除 Host 后不可复用该实例。
val messages = remember { MessageOverlayState() }
MessageHost(messages) {
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

Android 的原生提示调用复用 `AndroidMessagePlatform.get(applicationContext)`，避免各自展示竞争。iOS 使用不可点击的独立 UIWindow，需要有效根控制器；KMP 宿主通过 `IosMessageBridge` 转发到原生 presenter。HarmonyOS 需要已创建主窗口的 UIAbilityContext 或注册 `GycToastModule`。

每个 Pager 注册组件 `ToastModule` 和原生 `GycToastModule`，业务只映射消息文案/时长；`pageWillDestroy` 调用 `ToastModule.dispose()`，原生 `onDestroy` 后拒绝消息。
不再把showMessage协议复制到应用的系统动作Module；已显示Toast仍由唯一presenter管理，不由旧Page销毁新Page消息。

无需额外系统权限。空白文案不展示；仅支持短/长时长和新消息替换，不提供消息队列或交互式提示。原生窗口显示、替换和多窗口行为仍需宿主设备验收。

## 文档与帮助

- [接入指南](docs/接入指南.md)：平台初始化、权限声明和生命周期。
- [开发与验证](docs/开发与验证.md)：源码构建、检查命令与验收范围。
- [完整源码审查](docs/完整源码审查.md)
- [版本与发行说明](https://github.com/gycrosskit/toast/releases)、[问题反馈](https://github.com/gycrosskit/toast/issues)。

Apache-2.0，见 [LICENSE](LICENSE)。

## 自动回归

源码回归使用 `component-ci/verify`，见[统一远程验证（Jenkins）](https://github.com/gycrosskit/.github/blob/main/docs/统一远程验证.md)，按已推送源码 ref/SHA 选择对应组件；平台命令与参数以受信 Jenkins recipe 为准。实际执行范围、测试数量、结果与耗时以绑定源码/CI SHA 的构建回执为准，轻量检查或跳过不算平台测试通过。

新版本按[发布流程](https://github.com/gycrosskit/.github/blob/main/docs/发布流程.md)执行 `component-ci/release`。按渠道绑定准确的源码 SHA、版本和必要的 Tag，在同一次 Run 冻结候选并核对远端制品，保留发布和消费回执。

HAR 使用受管 HarmonyOS CLT 构建。KLIB 编译、Registry 安装及真机验收分别按实际回执记录。

缓存、重试和失败留痕见[共用 CI 规则](https://github.com/gycrosskit/.github/blob/main/docs/持续集成门禁.md)。

当前 `verification` 默认消费远程两个现存 Maven 产品；未发布候选可显式传 `-PverifyLocalSource` 验证本仓库源码，该模式不代表公开消费。Android `verifyNoCmpUi` 拒绝第二套 JetBrains/AndroidX CMP UI，允许 KuiklyCompose/runtime。`0.1.x` 的三产品历史验收应检出对应 tag 使用原 consumer，不用当前候选合同重验旧归档。
