package com.example.readiumandroidtestapp.core.utils

import kotlinx.coroutines.flow.Flow

interface UserMessageManager {
    val messages: Flow<Int>
    suspend fun emitMessage(messageId: Int)
}
