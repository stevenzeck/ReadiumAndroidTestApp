package com.example.readiumandroidtestapp.core.utils

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class UserMessageManagerTest {

    @Test
    fun `emitMessage emits message to flow`() = runTest {
        val manager = UserMessageManager()
        val messageId = 123
        val receivedMessages = mutableListOf<Int>()

        backgroundScope.launch(context = UnconfinedTestDispatcher(scheduler = testScheduler)) {
            manager.messages.collect {
                receivedMessages.add(it)
            }
        }

        manager.emitMessage(messageId = messageId)

        assertEquals(1, receivedMessages.size)
        assertEquals(messageId, receivedMessages[0])
    }
}
