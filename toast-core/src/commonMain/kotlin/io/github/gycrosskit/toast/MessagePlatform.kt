package io.github.gycrosskit.toast

/** 系统提示的时长档位；实际秒数由各原生 presenter 决定。 */
enum class AppMessageDuration {
    /** 短提示，适合操作反馈。 */
    SHORT,
    /** 长提示，适合需要更多阅读时间的文案。 */
    LONG,
}

/** 文案由调用方完成本地化；所有平台以新消息替换上一条。 */
fun interface MessagePlatform {
    /**
     * 原生实现负责切到 UI 线程，Kuikly 实现须在页面 Context 调用。空白文案忽略，不替换当前提示。
     * @param message 宿主已本地化的文本；去除首尾空白后展示。
     * @param duration 原生显示时长档位。
     */
    fun show(message: String, duration: AppMessageDuration)
}

/** 使用短时长展示 [message]，其余契约与 [MessagePlatform.show] 一致。 */
fun MessagePlatform.show(message: String) = show(message, AppMessageDuration.SHORT)
