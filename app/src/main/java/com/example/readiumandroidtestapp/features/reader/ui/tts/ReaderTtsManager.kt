package com.example.readiumandroidtestapp.features.reader.ui.tts

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import org.readium.navigator.media.tts.android.AndroidTtsEngine
import org.readium.navigator.media.tts.android.AndroidTtsPreferences
import org.readium.r2.navigator.VisualNavigator
import org.readium.r2.shared.publication.Publication

interface ReaderTtsManager {
    val isTtsActive: StateFlow<Boolean>
    val ttsPlayback: Flow<Boolean>
    val voices: Set<AndroidTtsEngine.Voice>

    fun initFactory(publication: Publication)
    fun start(visualNavigator: VisualNavigator, scope: CoroutineScope, onStop: () -> Unit)
    fun stop(visualNavigator: VisualNavigator?, scope: CoroutineScope)
    fun play()
    fun pause()
    fun previous()
    fun next()
    fun close()
    fun submitPreferences(preferences: AndroidTtsPreferences)
}
