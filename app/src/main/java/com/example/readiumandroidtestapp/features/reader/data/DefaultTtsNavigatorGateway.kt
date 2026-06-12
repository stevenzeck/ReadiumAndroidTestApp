package com.example.readiumandroidtestapp.features.reader.data

import com.example.readiumandroidtestapp.features.reader.domain.TtsNavigatorGateway
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import org.readium.navigator.media.tts.AndroidTtsNavigator
import org.readium.navigator.media.tts.TtsNavigator
import org.readium.navigator.media.tts.android.AndroidTtsEngine
import org.readium.navigator.media.tts.android.AndroidTtsPreferences
import org.readium.r2.shared.publication.Locator

class DefaultTtsNavigatorGateway(
    private val navigator: AndroidTtsNavigator,
) : TtsNavigatorGateway {

    override val playback: Flow<Boolean>
        get() = navigator.playback.map { it.playWhenReady }

    override val voices: Set<AndroidTtsEngine.Voice>
        get() = navigator.voices

    override val currentLocator: StateFlow<Locator>
        get() = navigator.currentLocator

    override val location: Flow<TtsNavigator.Location>
        get() = navigator.location


    override fun play() {
        navigator.play()
    }

    override fun pause() {
        navigator.pause()
    }

    override fun skipToPreviousUtterance() {
        navigator.skipToPreviousUtterance()
    }

    override fun skipToNextUtterance() {
        navigator.skipToNextUtterance()
    }

    override fun close() {
        navigator.close()
    }

    override fun submitPreferences(preferences: AndroidTtsPreferences) {
        navigator.submitPreferences(preferences)
    }
}
