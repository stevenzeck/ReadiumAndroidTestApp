package com.example.readiumandroidtestapp.features.reader.data

import android.app.Application
import com.example.readiumandroidtestapp.features.reader.domain.TtsNavigatorGateway
import com.example.readiumandroidtestapp.features.reader.domain.TtsServiceGateway
import org.readium.navigator.media.tts.AndroidTtsNavigatorFactory
import org.readium.navigator.media.tts.TtsNavigator
import org.readium.r2.shared.publication.Locator
import org.readium.r2.shared.publication.Publication
import javax.inject.Inject

class DefaultTtsServiceGateway @Inject constructor(
    private val application: Application,
) : TtsServiceGateway {

    override suspend fun createNavigator(
        publication: Publication,
        initialLocator: Locator,
        listener: TtsNavigatorGateway.Listener,
    ): Result<TtsNavigatorGateway> {
        val factory = AndroidTtsNavigatorFactory(
            application = application,
            publication = publication,
        ) ?: return Result.failure(Exception("Failed to create TTS Factory"))

        val ttsListener = object : TtsNavigator.Listener {
            override fun onStopRequested() {
                listener.onStopRequested()
            }
        }

        return factory.createNavigator(
            initialLocator = initialLocator,
            listener = ttsListener,
        ).fold(
            onSuccess = { navigator ->
                Result.success(value = DefaultTtsNavigatorGateway(navigator))
            },
            onFailure = { error ->
                Result.failure(Exception("TTS creation failed: $error"))
            },
        )
    }
}
