package com.example.readiumandroidtestapp.features.reader.ui.tts

import com.example.readiumandroidtestapp.features.reader.ui.state.ReaderNavigator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import org.readium.navigator.media.tts.android.AndroidTtsEngine
import org.readium.navigator.media.tts.android.AndroidTtsPreferences
import org.readium.r2.shared.publication.Publication

interface ReaderTtsManager {
    val isTtsActive: StateFlow<Boolean>
    val ttsPlayback: Flow<Boolean>
    val voices: Set<AndroidTtsEngine.Voice>

    fun initFactory(publication: Publication)
    fun start(navigator: ReaderNavigator, scope: CoroutineScope, onStop: () -> Unit)
    fun stop(navigator: ReaderNavigator?, scope: CoroutineScope)
    fun play()
    fun pause()
    fun previous()
    fun next()
    fun close()
    fun submitPreferences(preferences: AndroidTtsPreferences)
}
