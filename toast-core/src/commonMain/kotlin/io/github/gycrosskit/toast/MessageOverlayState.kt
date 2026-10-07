package io.github.gycrosskit.toast

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** 每个页面根 Host 拥有一个实例；两个 UI 引擎共用消息所有权，销毁后新建实例。 */
class MessageOverlayState : MessagePlatform {
    private val mutableState = MutableStateFlow(MessageOverlayUiState())
    val state = mutableState.asStateFlow()

    override fun show(message: String, duration: AppMessageDuration) {
        val text = message.trim()
        if (text.isEmpty()) return
        mutableState.update {
            if (it.disposed) it else it.copy(requestId = it.requestId + 1, text = text, duration = duration)
        }
    }

    /** 旧提示的定时关闭不能清掉后继消息，包括同一文案的再次展示。 */
    fun dismiss(requestId: Long) {
        mutableState.update { if (it.requestId == requestId) it.copy(text = null) else it }
    }

    fun dispose() {
        mutableState.update { it.copy(text = null, disposed = true) }
    }
}

data class MessageOverlayUiState(
    val requestId: Long = 0,
    val text: String? = null,
    val duration: AppMessageDuration = AppMessageDuration.SHORT,
    val disposed: Boolean = false,
)

/** 两个渲染引擎只转换单位和颜色，不另存样式或消息状态；根布局由宿主应用安全区域。 */
object MessageOverlaySpec {
    const val outerPaddingDp = 16
    const val horizontalPaddingDp = 18
    const val verticalPaddingDp = 12
    const val cornerRadiusDp = 12
    const val maxWidthDp = 520
    const val fontSizeSp = 14
    const val lineHeightSp = 20
    const val backgroundArgb = 0xD6000000
    const val textArgb = 0xFFFFFFFF
}
