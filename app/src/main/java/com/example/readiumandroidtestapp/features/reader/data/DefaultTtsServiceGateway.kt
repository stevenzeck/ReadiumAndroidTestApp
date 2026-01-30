package com.example.readiumandroidtestapp.features.reader.data

import android.app.Application
import com.example.readiumandroidtestapp.features.reader.domain.TtsNavigatorGateway
import com.example.readiumandroidtestapp.features.reader.domain.TtsServiceGateway
import org.readium.navigator.media.tts.TtsNavigator
import org.readium.r2.shared.publication.Locator
import org.readium.r2.shared.publication.Publication
import javax.inject.Inject

class DefaultTtsServiceGateway @Inject constructor(
    private val application: Application,
    private val factoryWrapper: AndroidTtsNavigatorFactoryWrapper,
) : TtsServiceGateway {

    override suspend fun createNavigator(
        publication: Publication,
        initialLocator: Locator,
        listener: TtsNavigatorGateway.Listener,
    ): Result<TtsNavigatorGateway> {
        val ttsListener = object : TtsNavigator.Listener {
            override fun onStopRequested() {
                listener.onStopRequested()
            }
        }

        return factoryWrapper.createNavigator(
            application = application,
            publication = publication,
            initialLocator = initialLocator,
            listener = ttsListener,
        ).map { navigator ->
            DefaultTtsNavigatorGateway(navigator)
        }
    }
}
