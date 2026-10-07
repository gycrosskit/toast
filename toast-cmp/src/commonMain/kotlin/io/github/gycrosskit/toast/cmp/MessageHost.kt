package io.github.gycrosskit.toast.cmp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.gycrosskit.toast.MessageOverlaySpec
import io.github.gycrosskit.toast.MessageOverlayState
import kotlinx.coroutines.delay

/** 放在已应用安全区域的页面根部；state 每个 Host 独占，移除 Host 会永久关闭该实例。 */
@Composable
fun MessageHost(state: MessageOverlayState, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val message by state.state.collectAsState()
    DisposableEffect(state) { onDispose { state.dispose() } }
    LaunchedEffect(state, message.requestId) {
        if (message.text != null) {
            delay(message.duration.milliseconds)
            state.dismiss(message.requestId)
        }
    }
    Box(modifier.fillMaxSize()) {
        ProvideMessagePlatform(state, content)
        message.text?.let { text ->
            BasicText(
                text = text,
                modifier = Modifier.align(Alignment.TopCenter)
                    .padding(MessageOverlaySpec.outerPaddingDp.dp)
                    .widthIn(max = MessageOverlaySpec.maxWidthDp.dp)
                    .background(Color(MessageOverlaySpec.backgroundArgb), RoundedCornerShape(MessageOverlaySpec.cornerRadiusDp.dp))
                    .padding(horizontal = MessageOverlaySpec.horizontalPaddingDp.dp, vertical = MessageOverlaySpec.verticalPaddingDp.dp)
                    .semantics { liveRegion = LiveRegionMode.Polite },
                style = TextStyle(color = Color(MessageOverlaySpec.textArgb), fontSize = MessageOverlaySpec.fontSizeSp.sp,
                    lineHeight = MessageOverlaySpec.lineHeightSp.sp, textAlign = TextAlign.Center),
            )
        }
    }
}
