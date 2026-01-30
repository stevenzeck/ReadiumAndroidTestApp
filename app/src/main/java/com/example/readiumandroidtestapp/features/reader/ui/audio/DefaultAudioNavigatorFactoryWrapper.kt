package com.example.readiumandroidtestapp.features.reader.ui.audio

import android.app.Application
import org.readium.adapter.exoplayer.audio.ExoPlayerEngineProvider
import org.readium.adapter.exoplayer.audio.ExoPlayerPreferences
import org.readium.adapter.exoplayer.audio.ExoPlayerSettings
import org.readium.navigator.media.audio.AudioNavigator
import org.readium.navigator.media.audio.AudioNavigatorFactory
import org.readium.r2.navigator.preferences.PreferencesEditor
import org.readium.r2.shared.publication.Locator
import org.readium.r2.shared.publication.Publication
import org.readium.r2.shared.util.Try
import javax.inject.Inject

class DefaultAudioNavigatorFactoryWrapper @Inject constructor() : AudioNavigatorFactoryWrapper {
    override suspend fun createNavigator(
        application: Application,
        publication: Publication,
        initialLocator: Locator?,
        initialPreferences: ExoPlayerPreferences?,
    ): Try<AudioNavigator<ExoPlayerSettings, ExoPlayerPreferences>, Exception> {
        val engineProvider = ExoPlayerEngineProvider(application)

        val factory =
            AudioNavigatorFactory(publication = publication, audioEngineProvider = engineProvider)
                ?: return Try.failure(
                    failure = Exception("Failed to create AudioNavigatorFactory: publication might not be supported"),
                )

        return factory.createNavigator(
            initialLocator = initialLocator,
            initialPreferences = initialPreferences,
        ).mapFailure { error ->
            Exception("Failed to create AudioNavigator: ${error.message}")
        }
    }

    override fun createPreferencesEditor(
        application: Application,
        publication: Publication,
        initialPreferences: ExoPlayerPreferences,
    ): PreferencesEditor<ExoPlayerPreferences>? {
        val engineProvider = ExoPlayerEngineProvider(application = application)

        val factory = AudioNavigatorFactory(publication, audioEngineProvider = engineProvider)
            ?: return null

        return factory.createAudioPreferencesEditor(currentPreferences = initialPreferences)
    }
}
