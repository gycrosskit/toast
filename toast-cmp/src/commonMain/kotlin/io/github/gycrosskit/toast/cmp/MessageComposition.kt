package io.github.gycrosskit.toast.cmp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import io.github.gycrosskit.toast.MessagePlatform

/** 宿主注入的提示入口；缺少 ProvideMessagePlatform 时读取会失败。 */
val LocalMessagePlatform = staticCompositionLocalOf<MessagePlatform> {
    error("MessagePlatform must be provided by the host")
}

/**
 * 向子组合提供提示能力；展示与生命周期由宿主拥有。
 * @param platform 各页面共用的原生提示实现。
 * @param content 使用该提示实现的子组合。
 */
@Composable
fun ProvideMessagePlatform(platform: MessagePlatform, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalMessagePlatform provides platform, content = content)
}

/** 读取当前组合的提示入口；不创建或缓存新的 presenter。 */
@Composable
fun rememberMessages(): MessagePlatform = LocalMessagePlatform.current
