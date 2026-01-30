package com.example.readiumandroidtestapp.features.reader.ui.audio

import android.app.Application
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import org.readium.adapter.exoplayer.audio.ExoPlayerPreferences
import org.readium.adapter.exoplayer.audio.ExoPlayerSettings
import org.readium.navigator.media.audio.AudioNavigator
import org.readium.r2.navigator.preferences.PreferencesEditor
import org.readium.r2.shared.publication.Locator
import org.readium.r2.shared.publication.Publication
import org.readium.r2.shared.util.Try
import javax.inject.Inject

class DefaultAppAudioNavigatorFactory @Inject constructor(
    @ApplicationContext private val context: Context,
    private val navigatorFactoryWrapper: AudioNavigatorFactoryWrapper,
) : AppAudioNavigatorFactory {
    override suspend fun createNavigator(
        publication: Publication,
        initialLocator: Locator?,
        initialPreferences: ExoPlayerPreferences?,
    ): Try<AudioNavigator<ExoPlayerSettings, ExoPlayerPreferences>, Exception> {
        val application = context as Application
        return navigatorFactoryWrapper.createNavigator(
            application = application,
            publication = publication,
            initialLocator = initialLocator,
            initialPreferences = initialPreferences,
        )
    }

    override fun createPreferencesEditor(
        publication: Publication,
        initialPreferences: ExoPlayerPreferences,
    ): PreferencesEditor<ExoPlayerPreferences>? {
        val application = context as Application
        return navigatorFactoryWrapper.createPreferencesEditor(
            application = application,
            publication = publication,
            initialPreferences = initialPreferences,
        )
    }
}
