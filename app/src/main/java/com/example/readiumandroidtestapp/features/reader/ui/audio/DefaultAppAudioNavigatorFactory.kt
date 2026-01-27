package com.example.readiumandroidtestapp.features.reader.ui.audio

import android.app.Application
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
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

class DefaultAppAudioNavigatorFactory @Inject constructor(
    @ApplicationContext private val context: Context,
) : AppAudioNavigatorFactory {
    override suspend fun createNavigator(
        publication: Publication,
        initialLocator: Locator?,
        initialPreferences: ExoPlayerPreferences?,
    ): Try<AudioNavigator<ExoPlayerSettings, ExoPlayerPreferences>, Exception> {
        val application = context as Application
        val engineProvider = ExoPlayerEngineProvider(application)

        // Create the factory using the Readium API.
        val factory =
            AudioNavigatorFactory(publication = publication, audioEngineProvider = engineProvider)
                ?: return Try.failure(
                    failure = Exception("Failed to create AudioNavigatorFactory: publication might not be supported"),
                )

        // Create the navigator and map the internal Readium Error to a standard Exception
        // so it can be handled generically by the ViewModel.
        return factory.createNavigator(
            initialLocator = initialLocator,
            initialPreferences = initialPreferences,
        ).mapFailure { error ->
            Exception("Failed to create AudioNavigator: ${error.message}")
        }
    }

    override fun createPreferencesEditor(
        publication: Publication,
        initialPreferences: ExoPlayerPreferences,
    ): PreferencesEditor<ExoPlayerPreferences>? {
        val application = context as Application
        val engineProvider = ExoPlayerEngineProvider(application = application)

        val factory = AudioNavigatorFactory(publication, audioEngineProvider = engineProvider)
            ?: return null

        return factory.createAudioPreferencesEditor(currentPreferences = initialPreferences)
    }
}
