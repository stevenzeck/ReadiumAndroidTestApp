package com.example.readiumandroidtestapp.features.reader.domain

import android.app.Application
import com.example.readiumandroidtestapp.features.reader.ui.state.ReaderNavigator
import com.example.readiumandroidtestapp.features.reader.ui.state.ReaderPreferences
import com.example.readiumandroidtestapp.features.reader.ui.state.ReaderPreferencesEditor
import com.example.readiumandroidtestapp.features.reader.ui.state.ReaderUiState
import com.example.readiumandroidtestapp.features.reader.ui.state.TtsSettingsSession
import com.example.readiumandroidtestapp.features.reader.ui.tts.ReaderTtsManager
import org.readium.adapter.exoplayer.audio.ExoPlayerPreferences
import org.readium.navigator.media.audio.AudioNavigator
import org.readium.r2.shared.publication.Publication

interface ReaderPreferencesManager {
    suspend fun commitPreferences(
        bookId: Long,
        preferences: ReaderPreferences,
        publication: Publication,
        navigator: ReaderNavigator?,
        audioNavigator: AudioNavigator<*, *>?,
        ttsManager: ReaderTtsManager,
    )

    fun createPreferencesEditor(
        publication: Publication,
        preferences: ReaderPreferences,
    ): ReaderPreferencesEditor?

    suspend fun loadPreferences(
        bookId: Long,
        publication: Publication,
    ): ReaderPreferences

    suspend fun loadAudiobookPreferences(bookId: Long): ExoPlayerPreferences

    suspend fun createTtsSettingsSession(
        bookId: Long,
        publication: Publication,
        ttsManager: ReaderTtsManager,
        application: Application,
    ): TtsSettingsSession?

    fun refreshSessionState(
        currentState: ReaderUiState,
        newPreferences: ReaderPreferences,
    ): ReaderUiState?
}
