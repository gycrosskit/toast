package io.github.gycrosskit.toast

/** iOS 原生展示由同仓库的 GycToastNative Swift Package 提供。 */
interface IosMessageBridge {
    fun showMessage(message: String, longDuration: Boolean)
}

class IosMessagePlatform(private val bridge: IosMessageBridge) : MessagePlatform {
    override fun show(message: String, duration: AppMessageDuration) {
        val text = message.trim()
        if (text.isEmpty()) return
        bridge.showMessage(text, duration == AppMessageDuration.LONG)
    }
}
