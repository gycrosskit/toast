package io.github.gycrosskit.toast.kuikly

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.tencent.kuikly.core.render.android.IKuiklyRenderExport
import com.tencent.kuikly.core.render.android.export.KuiklyRenderBaseModule
import com.tencent.kuikly.core.render.android.export.KuiklyRenderCallback
import io.github.gycrosskit.toast.AndroidMessagePlatform
import io.github.gycrosskit.toast.AppMessageDuration
import io.github.gycrosskit.toast.MessagePlatform
import org.json.JSONException
import org.json.JSONObject

/** 每个 Renderer 注册一个接收端，显示仍复用 CMP 使用的进程提示入口。 */
class AndroidToastModule(private val platform: MessagePlatform) : KuiklyRenderBaseModule() {
    constructor(context: Context) : this(AndroidMessagePlatform.get(context))

    private val mainHandler = Handler(Looper.getMainLooper())
    @Volatile private var destroyed = false

    override fun call(method: String, params: String?, callback: KuiklyRenderCallback?): Any? {
        if (destroyed || method != "show" || params == null) return null
        val values = try { JSONObject(params) } catch (_: JSONException) { return null }
        val message = (values.opt("message") as? String)?.trim().orEmpty()
        if (message.isEmpty()) return null
        val duration = when (values.opt("duration")) {
            "SHORT" -> AppMessageDuration.SHORT
            "LONG" -> AppMessageDuration.LONG
            else -> return null
        }
        val display = Runnable { if (!destroyed) platform.show(message, duration) }
        if (Looper.myLooper() == Looper.getMainLooper()) display.run() else mainHandler.post(display)
        return null
    }

    override fun onDestroy() {
        destroyed = true
        mainHandler.removeCallbacksAndMessages(null)
    }
}

/** 在当前 Renderer 的 registerExternalModule 中调用；不复用其他 Renderer 的接收端。 */
fun IKuiklyRenderExport.registerToastModule(context: Context) {
    val platform = AndroidMessagePlatform.get(context)
    moduleExport(ToastModule.NAME) { AndroidToastModule(platform) }
}
