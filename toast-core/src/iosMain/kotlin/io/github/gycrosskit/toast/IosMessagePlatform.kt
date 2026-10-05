package io.github.gycrosskit.toast

/** iOS 原生展示由同仓库的 GycToastNative Swift Package 提供。 */
interface IosMessageBridge {
    /**
     * 可从任意线程调用；Swift presenter 负责切到主线程并替换旧提示。
     * @param message 已去除首尾空白的非空文本。
     * @param longDuration true 使用长时长，false 使用短时长。
     */
    fun showMessage(message: String, longDuration: Boolean)
}

/**
 * 将 KMP 文案交给宿主提供的原生桥，不持有控制器。
 * @param bridge 宿主长期持有的 Swift presenter 桥；所有页面应共用原生 presenter。
 */
class IosMessagePlatform(private val bridge: IosMessageBridge) : MessagePlatform {
    override fun show(message: String, duration: AppMessageDuration) {
        val text = message.trim()
        if (text.isEmpty()) return
        bridge.showMessage(text, duration == AppMessageDuration.LONG)
    }
}
