package io.github.gycrosskit.toast.kuikly

import android.os.Looper
import io.github.gycrosskit.toast.AppMessageDuration
import io.github.gycrosskit.toast.MessagePlatform
import java.util.concurrent.FutureTask
import java.util.concurrent.TimeUnit
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], manifest = Config.NONE)
@LooperMode(LooperMode.Mode.PAUSED)
class AndroidToastModuleTest {
    @Test fun sdkCallRunsOnMainAndPreservesMessageAndDuration() {
        val calls = mutableListOf<Pair<String, AppMessageDuration>>()
        val receiver = AndroidToastModule(object : MessagePlatform {
            override fun show(message: String, duration: AppMessageDuration) {
                assertEquals(Looper.getMainLooper(), Looper.myLooper())
                calls += message to duration
            }
        })
        background { receiver.call("show", """{"message":" hello ","duration":"LONG"}""", null) }
        assertTrue(calls.isEmpty())
        shadowOf(Looper.getMainLooper()).idle()
        assertEquals(listOf("hello" to AppMessageDuration.LONG), calls)
    }

    @Test fun destroyDropsQueuedCallsAndMalformedInputNeverReachesPlatform() {
        var calls = 0
        val receiver = AndroidToastModule(object : MessagePlatform {
            override fun show(message: String, duration: AppMessageDuration) { calls++ }
        })
        listOf("broken", "{}", """{"message":12,"duration":"SHORT"}""",
            """{"message":"hello","duration":"unknown"}""",
            """{"message":"  ","duration":"LONG"}""").forEach { receiver.call("show", it, null) }
        receiver.call("unknown", """{"message":"hello","duration":"SHORT"}""", null)
        background { receiver.call("show", """{"message":"queued","duration":"SHORT"}""", null) }
        receiver.onDestroy()
        shadowOf(Looper.getMainLooper()).idle()
        receiver.call("show", """{"message":"late","duration":"SHORT"}""", null)
        assertEquals(0, calls)
    }

    private fun background(block: () -> Unit) {
        val task = FutureTask<Unit> { block() }
        Thread(task).start()
        task.get(5, TimeUnit.SECONDS)
    }
}
