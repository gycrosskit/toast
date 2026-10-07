package io.github.gycrosskit.toast

import kotlin.test.*

class MessageOverlayStateTest {
    @Test fun latestMessageOwnsDismissalAndDisposeIsTerminal() {
        val messages = MessageOverlayState()
        messages.show(" old ")
        val first = messages.state.value.requestId
        messages.show("new", AppMessageDuration.LONG)
        val second = messages.state.value.requestId
        messages.dismiss(first)
        assertEquals("new", messages.state.value.text)
        messages.show(" \n\t")
        assertEquals(second, messages.state.value.requestId)
        messages.show("new")
        messages.dismiss(second)
        assertEquals("new", messages.state.value.text)
        messages.dismiss(messages.state.value.requestId)
        assertNull(messages.state.value.text)
        messages.dispose()
        messages.show("cannot reopen")
        assertNull(messages.state.value.text)
        assertTrue(messages.state.value.disposed)
        assertEquals(2_000L, AppMessageDuration.SHORT.milliseconds)
        assertEquals(3_500L, AppMessageDuration.LONG.milliseconds)
    }
}
