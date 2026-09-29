# GY CrossKit Toast

Android、iOS、HarmonyOS 共用的短消息出口。新消息替换旧消息；文案由应用本地化。KMP 的 `MessagePlatform` 是业务入口，CMP 使用 `toast-cmp` 的 CompositionLocal，鸿蒙 Kuikly 使用 `toast-kuikly` 的 Module。原生展示分别在 Android AAR、iOS `GycToastNative` 和鸿蒙 `@gycrosskit/toast-native` HAR。

## KMP

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositories { maven { url = uri("https://jitpack.io") } }
}

// commonMain.dependencies
implementation("com.github.gycrosskit.toast:toast:0.1.2")
// CMP 宿主另外引入 com.github.gycrosskit.toast:toast-cmp:0.1.2
// Kuikly 宿主另外引入 com.github.gycrosskit.toast:toast-kuikly:0.1.2
```

CMP 在应用根部 `ProvideMessagePlatform(platform)`；页面调用 `rememberMessages().show("...")`。Android 宿主使用 `AndroidMessagePlatform.get(applicationContext)`，两个 UI 引擎必须复用这一实例。iOS 宿主使用 `IosMessagePlatform(bridge)`，Swift bridge 转发给 `GycToastPresenter.shared.show`。

当前 KMP 产物为鸿蒙目标使用 Kuikly Kotlin `2.2.21-1.0.0` 编译。接入工程需在 Gradle `pluginManagement` 与依赖仓库中包含 `https://maven.eazytec-cloud.com/nexus/repository/maven-public/`，并使用兼容的 Kotlin/Kuikly 版本。

## iOS 原生依赖

纯 Swift 工程可通过 Xcode 的 Swift Package Dependencies 添加 `https://github.com/gycrosskit/toast.git`，选择版本 `0.1.2`，产品为 `GycToastNative`。使用 CocoaPods 的宿主可写：

```ruby
pod 'GycToastNative', :git => 'https://github.com/gycrosskit/toast.git', :tag => '0.1.2'
```

在稳定的根 UIViewController 创建后调用 `GycToastPresenter.shared.bind(rootController:)`；组件使用独立、不可点击的 UIWindow，不依附临时 dialog window。CMP 的 iOS KLIB 仍通过 Gradle/Maven 获取。

## 鸿蒙原生依赖

`ohos/toast-native` 构建为 HAR，使用 API 18 起提供的 `PromptAction.openToast/closeToast`。目标版本为 `0.1.2`；先确认 ohpm 审核通过且能从仓库查询，再以 `"@gycrosskit/toast-native": "0.1.2"` 引入并注册 `GycToastModule`。Kuikly 的 `toast-kuikly` KLIB 仍通过 Gradle/JitPack 获取。发布前可用本地 HAR 验证，但不能将本地路径作为最终远程依赖。

JitPack 提供 Maven 产物，不能替代 Swift Package 的 Git 标签或鸿蒙 ohpm 的 HAR 分发。

## 构建

```bash
VERSION=0.1.2 bash gradlew publishToMavenLocal
xcodebuild -scheme GycToastNative -destination 'generic/platform=iOS Simulator' -sdk iphonesimulator build CODE_SIGNING_ALLOWED=NO
cd ohos && DEVECO_SDK_HOME=/Applications/DevEco-Studio.app/Contents/sdk /Applications/DevEco-Studio.app/Contents/tools/hvigor/bin/hvigorw assembleHar --no-daemon
```

KMP Maven 产物由 JitPack 按 Git 标签构建。发布新版本时同步更新 podspec 版本、oh-package 版本和 Git 标签。

Apache-2.0，见 [LICENSE](LICENSE)。
