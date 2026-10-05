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

        platform.show("  \n\t", AppMessageDuration.SHORT)
        platform.show(" 短提示 ", AppMessageDuration.SHORT)
        platform.show("长提示", AppMessageDuration.LONG)
        platform.show(" 默认提示 ")

        assertEquals(listOf("短提示" to false, "长提示" to true, "默认提示" to false), calls)
    }
}
