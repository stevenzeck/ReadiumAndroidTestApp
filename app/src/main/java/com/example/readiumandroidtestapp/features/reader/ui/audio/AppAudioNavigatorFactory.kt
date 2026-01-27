package com.example.readiumandroidtestapp.features.reader.ui.audio

import org.readium.adapter.exoplayer.audio.ExoPlayerPreferences
import org.readium.adapter.exoplayer.audio.ExoPlayerSettings
import org.readium.navigator.media.audio.AudioNavigator
import org.readium.r2.navigator.preferences.PreferencesEditor
import org.readium.r2.shared.publication.Locator
import org.readium.r2.shared.publication.Publication
import org.readium.r2.shared.util.Try

interface AppAudioNavigatorFactory {
    suspend fun createNavigator(
        publication: Publication,
        initialLocator: Locator?,
        initialPreferences: ExoPlayerPreferences? = null,
    ): Try<AudioNavigator<ExoPlayerSettings, ExoPlayerPreferences>, Exception>

    fun createPreferencesEditor(
        publication: Publication,
        initialPreferences: ExoPlayerPreferences,
    ): PreferencesEditor<ExoPlayerPreferences>?
}
