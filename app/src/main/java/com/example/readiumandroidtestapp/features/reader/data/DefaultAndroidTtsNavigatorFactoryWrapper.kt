package com.example.readiumandroidtestapp.features.reader.data

import android.app.Application
import org.readium.navigator.media.tts.AndroidTtsNavigator
import org.readium.navigator.media.tts.AndroidTtsNavigatorFactory
import org.readium.navigator.media.tts.TtsNavigator
import org.readium.r2.shared.publication.Locator
import org.readium.r2.shared.publication.Publication
import javax.inject.Inject

class DefaultAndroidTtsNavigatorFactoryWrapper @Inject constructor() :
    AndroidTtsNavigatorFactoryWrapper {

    override suspend fun createNavigator(
        application: Application,
        publication: Publication,
        initialLocator: Locator,
        listener: TtsNavigator.Listener,
    ): Result<AndroidTtsNavigator> {
        val factory = AndroidTtsNavigatorFactory(
            application = application,
            publication = publication,
        ) ?: return Result.failure(Exception("Failed to create TTS Factory"))

        return factory.createNavigator(
            initialLocator = initialLocator,
            listener = listener,
        ).fold(
            onSuccess = { Result.success(value = it) },
            onFailure = { Result.failure(Exception("TTS creation failed: $it")) },
        )
    }

    override suspend fun createFactory(
        application: Application,
        publication: Publication,
    ): AndroidTtsNavigatorFactory? {
        return AndroidTtsNavigatorFactory(application, publication)
    }
}
