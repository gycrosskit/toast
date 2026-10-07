package io.github.gycrosskit.toast.kuikly

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import io.github.gycrosskit.toast.MessagePlatform

/** Kuikly Compose 与 CMP 使用相同 MessagePlatform 合同，渲染类型保持各框架的薄边界。 */
val LocalMessagePlatform = staticCompositionLocalOf<MessagePlatform> {
    error("MessagePlatform must be provided by the host")
}

@Composable
fun ProvideMessagePlatform(platform: MessagePlatform, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalMessagePlatform provides platform, content = content)
}

@Composable
fun rememberMessages(): MessagePlatform = LocalMessagePlatform.current
