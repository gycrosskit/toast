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

    fun dispose() {
        disposed = true
    }

    companion object { const val NAME = "GycToastModule" }
}
