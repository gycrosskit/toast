package consumer

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.gycrosskit.toast.MessageOverlayState
import io.github.gycrosskit.toast.cmp.MessageHost
import io.github.gycrosskit.toast.cmp.rememberMessages

@Composable fun unifiedCmpMessages(state: MessageOverlayState) {
    MessageHost(state, Modifier) { rememberMessages() }
}
