package consumer

import android.content.Context
import androidx.compose.runtime.Composable
import io.github.gycrosskit.toast.AndroidMessagePlatform
import io.github.gycrosskit.toast.MessagePlatform
import io.github.gycrosskit.toast.cmp.ProvideMessagePlatform
import io.github.gycrosskit.toast.cmp.rememberMessages

fun create(context: Context) = AndroidMessagePlatform.get(context)
@Composable fun messages(platform: MessagePlatform) {
    ProvideMessagePlatform(platform) { rememberMessages() }
}
