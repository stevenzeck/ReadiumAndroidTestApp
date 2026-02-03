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

class DefaultAudioNavigatorFactoryWrapper(
    private val createEngineProvider: (Application) -> ExoPlayerEngineProvider,
    private val createNavigator: suspend (Publication, ExoPlayerEngineProvider, Locator?, ExoPlayerPreferences?) -> Try<AudioNavigator<ExoPlayerSettings, ExoPlayerPreferences>, Exception>,
    private val createPreferencesEditor: (Publication, ExoPlayerEngineProvider, ExoPlayerPreferences) -> PreferencesEditor<ExoPlayerPreferences>?,
) : AudioNavigatorFactoryWrapper {

    @Inject
    constructor() : this(
        createEngineProvider = { application -> ExoPlayerEngineProvider(application = application) },
        createNavigator = { publication, engineProvider, locator, preferences ->
            val factory = AudioNavigatorFactory(
                publication = publication,
                audioEngineProvider = engineProvider,
            )
            factory?.createNavigator(initialLocator = locator, initialPreferences = preferences)
                ?.mapFailure { error ->
                    Exception("Failed to create AudioNavigator: ${error.message}")
                } ?: Try.failure(
                failure = Exception("Failed to create AudioNavigatorFactory: publication might not be supported"),
            )
        },
        createPreferencesEditor = { publication, engineProvider, preferences ->
            AudioNavigatorFactory(
                publication = publication,
                audioEngineProvider = engineProvider,
            )?.createAudioPreferencesEditor(currentPreferences = preferences)
        },
    )

    override suspend fun createNavigator(
        application: Application,
        publication: Publication,
        initialLocator: Locator?,
        initialPreferences: ExoPlayerPreferences?,
    ): Try<AudioNavigator<ExoPlayerSettings, ExoPlayerPreferences>, Exception> {
        val engineProvider = createEngineProvider(application)
        return createNavigator(publication, engineProvider, initialLocator, initialPreferences)
    }

    override fun createPreferencesEditor(
        application: Application,
        publication: Publication,
        initialPreferences: ExoPlayerPreferences,
    ): PreferencesEditor<ExoPlayerPreferences>? {
        val engineProvider = createEngineProvider(application)
        return createPreferencesEditor(publication, engineProvider, initialPreferences)
    }
}
