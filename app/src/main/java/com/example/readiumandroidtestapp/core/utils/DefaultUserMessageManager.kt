package com.example.readiumandroidtestapp.core.utils

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultUserMessageManager @Inject constructor() : UserMessageManager {
    private val _messages = Channel<Int>(capacity = Channel.BUFFERED)
    override val messages: Flow<Int> = _messages.receiveAsFlow()

    override suspend fun emitMessage(messageId: Int) {
        _messages.send(element = messageId)
    }
}
