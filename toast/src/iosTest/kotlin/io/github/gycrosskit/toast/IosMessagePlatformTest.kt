package io.github.gycrosskit.toast

import kotlin.test.Test
import kotlin.test.assertEquals

class IosMessagePlatformTest {
    @Test
    fun blankMessagesAreIgnoredAndDurationIsForwarded() {
        val calls = mutableListOf<Pair<String, Boolean>>()
        val platform = IosMessagePlatform(object : IosMessageBridge {
            override fun showMessage(message: String, longDuration: Boolean) {
                calls += message to longDuration
            }
        })

        platform.show("  ", AppMessageDuration.SHORT)
        platform.show(" 短提示 ", AppMessageDuration.SHORT)
        platform.show("长提示", AppMessageDuration.LONG)

        assertEquals(listOf("短提示" to false, "长提示" to true), calls)
    }
}
