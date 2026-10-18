package io.github.gycrosskit.toast.kuikly

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import io.github.gycrosskit.toast.MessagePlatform

/** Kuikly Compose 注入宿主提供的 MessagePlatform，不持有额外消息状态。 */
val LocalMessagePlatform = staticCompositionLocalOf<MessagePlatform> {
    error("MessagePlatform must be provided by the host")
}

@Composable
fun ProvideMessagePlatform(platform: MessagePlatform, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalMessagePlatform provides platform, content = content)
}

@Composable
fun rememberMessages(): MessagePlatform = LocalMessagePlatform.current
