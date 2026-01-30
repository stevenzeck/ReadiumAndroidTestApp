package com.example.readiumandroidtestapp.core.utils

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class UserMessageManagerTest {

    private val manager = DefaultUserMessageManager()

    @Test
    fun `emitMessage sends message to flow`() = runTest {
        val messageId = 123
        manager.emitMessage(messageId = messageId)
        val result = manager.messages.first()
        assertEquals(messageId, result)
    }
}
