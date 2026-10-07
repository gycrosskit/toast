package io.github.gycrosskit.toast.kuikly

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.foundation.layout.widthIn
import com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape
import com.tencent.kuikly.compose.foundation.text.BasicText
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.semantics.LiveRegionMode
import com.tencent.kuikly.compose.ui.semantics.liveRegion
import com.tencent.kuikly.compose.ui.semantics.semantics
import com.tencent.kuikly.compose.ui.text.TextStyle
import com.tencent.kuikly.compose.ui.text.style.TextAlign
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
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
