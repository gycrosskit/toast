package consumer

import android.content.Context
import com.tencent.kuikly.core.render.android.IKuiklyRenderExport
import io.github.gycrosskit.toast.kuikly.AndroidToastModule
import io.github.gycrosskit.toast.kuikly.registerToastModule

fun registerNativeToast(renderer: IKuiklyRenderExport, context: Context) {
    renderer.registerToastModule(context)
    AndroidToastModule(context).onDestroy()
}
