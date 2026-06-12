package com.example.readiumandroidtestapp.features.reader.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import org.readium.navigator.media.tts.TtsNavigator
import org.readium.navigator.media.tts.android.AndroidTtsEngine
import org.readium.navigator.media.tts.android.AndroidTtsPreferences
import org.readium.r2.shared.publication.Locator

interface TtsNavigatorGateway {
    val playback: Flow<Boolean>
    val voices: Set<AndroidTtsEngine.Voice>
    val currentLocator: StateFlow<Locator>
    val location: Flow<TtsNavigator.Location>

    fun play()
    fun pause()
    fun skipToPreviousUtterance()
    fun skipToNextUtterance()
    fun close()
    fun submitPreferences(preferences: AndroidTtsPreferences)

    interface Listener {
        fun onStopRequested()
    }
}
