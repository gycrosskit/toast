package io.github.gycrosskit.toast.kuikly

import com.tencent.kuikly.core.module.Module
import com.tencent.kuikly.core.nvi.serialization.json.JSONObject
import io.github.gycrosskit.toast.AppMessageDuration
import io.github.gycrosskit.toast.MessagePlatform

/** Kuikly 只负责转发，原生展示由各平台的 toast-native 依赖持有。 */
class ToastModule : Module(), MessagePlatform {
    private var disposed = false
    override fun moduleName(): String = NAME

    override fun show(message: String, duration: AppMessageDuration) {
        if (disposed) return
        val text = message.trim()
        if (text.isEmpty()) return
        asyncToNativeMethod("show", JSONObject().apply {
            put("message", text)
            put("duration", duration.name)
        }, null)
    }

    /** 页面销毁时在 Kuikly Context 调用；幂等，后续 show 忽略，不关闭其他页面提示。 */
    fun dispose() {
        disposed = true
    }

    companion object { /** 与原生注册名一致的桥名称。 */ const val NAME = "GycToastModule" }
}
