package com.example.readiumandroidtestapp.core.utils

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserMessageManager @Inject constructor() {
    private val _messages = Channel<Int>(capacity = Channel.BUFFERED)
    val messages = _messages.receiveAsFlow()

    suspend fun emitMessage(messageId: Int) {
        _messages.send(element = messageId)
    }
}
