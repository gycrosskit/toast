package io.github.gycrosskit.toast

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import java.util.concurrent.atomic.AtomicLong

/** 应用进程只持有一个实例，CMP 与 Kuikly 的原生桥必须共用它。 */
class AndroidMessagePlatform private constructor(context: Context) : MessagePlatform {
    private val appContext = context.applicationContext
    private val mainHandler = Handler(Looper.getMainLooper())
    private val requestId = AtomicLong()
    private var activeToast: Toast? = null

    override fun show(message: String, duration: AppMessageDuration) {
        val text = message.trim()
        if (text.isEmpty()) return
        val currentRequest = requestId.incrementAndGet()
        val display = Runnable {
            if (currentRequest != requestId.get()) return@Runnable
            activeToast?.cancel()
            activeToast = Toast.makeText(
                appContext,
                text,
                if (duration == AppMessageDuration.LONG) Toast.LENGTH_LONG else Toast.LENGTH_SHORT,
            ).also(Toast::show)
        }
        if (Looper.myLooper() == Looper.getMainLooper()) display.run() else mainHandler.post(display)
    }

    companion object {
        @Volatile private var instance: AndroidMessagePlatform? = null

        fun get(context: Context): AndroidMessagePlatform =
            instance ?: synchronized(this) {
                instance ?: AndroidMessagePlatform(context).also { instance = it }
            }
    }
}
