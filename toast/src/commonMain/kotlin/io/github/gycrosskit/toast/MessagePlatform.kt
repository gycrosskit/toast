package io.github.gycrosskit.toast

enum class AppMessageDuration { SHORT, LONG }

/** 文案由调用方完成本地化；所有平台以新消息替换上一条。 */
fun interface MessagePlatform {
    fun show(message: String, duration: AppMessageDuration)
}

fun MessagePlatform.show(message: String) = show(message, AppMessageDuration.SHORT)
