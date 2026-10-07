package consumer

import androidx.compose.runtime.Composable
import com.tencent.kuikly.compose.ui.Modifier
import io.github.gycrosskit.toast.MessageOverlayState
import io.github.gycrosskit.toast.kuikly.MessageHost
import io.github.gycrosskit.toast.kuikly.rememberMessages

@Composable fun unifiedKuiklyMessages(state: MessageOverlayState) {
    MessageHost(state, Modifier) { rememberMessages() }
}
