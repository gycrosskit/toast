package io.github.gycrosskit.toast

import android.os.Looper
import android.widget.Toast
import java.util.concurrent.FutureTask
import java.util.concurrent.TimeUnit
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import org.robolectric.shadows.ShadowToast
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], manifest = Config.NONE)
@LooperMode(LooperMode.Mode.PAUSED)
class AndroidMessagePlatformTest {
    private lateinit var platform: AndroidMessagePlatform

    @Before fun setup() {
        // 测试间重建 Robolectric Application，不能复用前一测试的进程 Context/Handler。
        AndroidMessagePlatform::class.java.getDeclaredField("instance").apply { isAccessible = true }.set(null, null)
        ShadowToast.reset()
        platform = AndroidMessagePlatform.get(RuntimeEnvironment.getApplication())
    }

    @Test fun backgroundQueueDisplaysOnlyNewestRequestAfterMainRuns() {
        background {
            platform.show("old", AppMessageDuration.SHORT)
            platform.show(" latest ", AppMessageDuration.LONG)
        }
        assertEquals(0, ShadowToast.shownToastCount())
        shadowOf(Looper.getMainLooper()).idle()
        assertEquals(1, ShadowToast.shownToastCount())
        assertEquals("latest", ShadowToast.getTextOfLatestToast())
        assertEquals(Toast.LENGTH_LONG, ShadowToast.getLatestToast().duration)
    }

    @Test fun mainRequestCancelsPreviousToastAndUsesNativeDuration() {
        platform.show("short", AppMessageDuration.SHORT)
        val previous = ShadowToast.getLatestToast()
        assertEquals(Toast.LENGTH_SHORT, previous.duration)
        assertFalse(shadowOf(previous).isCancelled)
        platform.show("long", AppMessageDuration.LONG)
        assertTrue(shadowOf(previous).isCancelled)
        assertEquals(2, ShadowToast.shownToastCount())
        assertEquals("long", ShadowToast.getTextOfLatestToast())
        assertEquals(Toast.LENGTH_LONG, ShadowToast.getLatestToast().duration)
    }

    @Test fun blankMainAndBackgroundMessagesDoNotReplaceActiveOrInvalidateQueuedRequest() {
        platform.show("visible")
        val previous = ShadowToast.getLatestToast()
        platform.show(" \n\t", AppMessageDuration.LONG)
        background { platform.show(" \t", AppMessageDuration.LONG) }
        shadowOf(Looper.getMainLooper()).idle()
        assertEquals(1, ShadowToast.shownToastCount())
        assertSame(previous, ShadowToast.getLatestToast())
        assertFalse(shadowOf(previous).isCancelled)

        background { platform.show("queued"); platform.show(" \n\t", AppMessageDuration.LONG) }
        shadowOf(Looper.getMainLooper()).idle()
        assertEquals(2, ShadowToast.shownToastCount())
        assertEquals("queued", ShadowToast.getTextOfLatestToast())
        assertEquals(Toast.LENGTH_SHORT, ShadowToast.getLatestToast().duration)
        assertTrue(shadowOf(previous).isCancelled)
    }

    @Test fun newerMainRequestSupersedesOlderBackgroundWork() {
        background { platform.show("queued old", AppMessageDuration.LONG) }
        platform.show("main newest")
        val current = ShadowToast.getLatestToast()
        shadowOf(Looper.getMainLooper()).idle()
        assertEquals(1, ShadowToast.shownToastCount())
        assertSame(current, ShadowToast.getLatestToast())
        assertEquals("main newest", ShadowToast.getTextOfLatestToast())
        assertFalse(shadowOf(current).isCancelled)
    }

    private fun background(block: () -> Unit) {
        val task = FutureTask<Unit> { block() }
        Thread(task).start()
        task.get(5, TimeUnit.SECONDS)
    }
}
