package consumer

import io.github.gycrosskit.toast.AppMessageDuration
import io.github.gycrosskit.toast.kuikly.ToastModule

fun createOhos() = ToastModule().apply { show("probe", AppMessageDuration.LONG) }
fun destroy(module: ToastModule) = module.dispose()
