package consumer

import androidx.compose.runtime.Composable
import io.github.gycrosskit.toast.IosMessageBridge
import io.github.gycrosskit.toast.IosMessagePlatform
import io.github.gycrosskit.toast.MessagePlatform
import io.github.gycrosskit.toast.cmp.ProvideMessagePlatform
import io.github.gycrosskit.toast.cmp.rememberMessages

fun create(bridge: IosMessageBridge) = IosMessagePlatform(bridge)
@Composable fun messages(platform: MessagePlatform) {
    ProvideMessagePlatform(platform) { rememberMessages() }
}
