package com.example.readiumandroidtestapp.features.reader.ui.audio

import android.app.Application
import org.readium.adapter.exoplayer.audio.ExoPlayerPreferences
import org.readium.adapter.exoplayer.audio.ExoPlayerSettings
import org.readium.navigator.media.audio.AudioNavigator
import org.readium.r2.navigator.preferences.PreferencesEditor
import org.readium.r2.shared.publication.Locator
import org.readium.r2.shared.publication.Publication
import org.readium.r2.shared.util.Try

interface AudioNavigatorFactoryWrapper {
    suspend fun createNavigator(
        application: Application,
        publication: Publication,
        initialLocator: Locator?,
        initialPreferences: ExoPlayerPreferences?,
    ): Try<AudioNavigator<ExoPlayerSettings, ExoPlayerPreferences>, Exception>

    fun createPreferencesEditor(
        application: Application,
        publication: Publication,
        initialPreferences: ExoPlayerPreferences,
    ): PreferencesEditor<ExoPlayerPreferences>?
}
