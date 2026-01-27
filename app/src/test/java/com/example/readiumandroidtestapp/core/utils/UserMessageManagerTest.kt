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
    fun `emitMessage sends message to flow`() = runTest {
        val manager = UserMessageManager()
        val expectedMessageId = 123

        val receivedMessages = mutableListOf<Int>()
        val job = launch(context = UnconfinedTestDispatcher(testScheduler)) {
            manager.messages.collect { receivedMessages.add(it) }
        }

        manager.emitMessage(expectedMessageId)

        assertEquals(expectedMessageId, receivedMessages.first())

        job.cancel()
    }
}
