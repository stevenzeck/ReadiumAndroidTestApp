package com.example.readiumandroidtestapp.features.reader.domain

import android.app.Application
import com.example.readiumandroidtestapp.features.reader.ui.state.ReaderUiState
import com.example.readiumandroidtestapp.features.reader.ui.state.TtsSettingsSession
import com.example.readiumandroidtestapp.features.reader.ui.tts.ReaderTtsManager
import org.readium.adapter.exoplayer.audio.ExoPlayerPreferences
import org.readium.navigator.media.audio.AudioNavigator
import org.readium.r2.navigator.VisualNavigator
import org.readium.r2.navigator.preferences.Configurable
import org.readium.r2.navigator.preferences.PreferencesEditor
import org.readium.r2.shared.publication.Publication

interface ReaderPreferencesManager {
    suspend fun commitPreferences(
        bookId: Long,
        preferences: Configurable.Preferences<*>,
        currentVisualNavigator: VisualNavigator?,
        audioNavigator: AudioNavigator<*, *>?,
        ttsManager: ReaderTtsManager,
    )

    fun createPreferencesEditor(
        publication: Publication,
        preferences: Configurable.Preferences<*>,
    ): PreferencesEditor<*>?

    suspend fun loadPreferences(
        bookId: Long,
        publication: Publication,
    ): Configurable.Preferences<*>

    suspend fun loadAudiobookPreferences(bookId: Long): ExoPlayerPreferences

    suspend fun createTtsSettingsSession(
        bookId: Long,
        publication: Publication,
        ttsManager: ReaderTtsManager,
        application: Application,
    ): TtsSettingsSession?

    fun refreshSessionState(
        currentState: ReaderUiState,
        newPreferences: Configurable.Preferences<*>,
    ): ReaderUiState?
}
