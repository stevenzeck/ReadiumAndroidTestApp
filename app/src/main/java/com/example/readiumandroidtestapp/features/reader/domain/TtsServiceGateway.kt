package com.example.readiumandroidtestapp.features.reader.domain

import org.readium.r2.shared.publication.Locator
import org.readium.r2.shared.publication.Publication

interface TtsServiceGateway {
    suspend fun createNavigator(
        publication: Publication,
        initialLocator: Locator,
        listener: TtsNavigatorGateway.Listener,
    ): Result<TtsNavigatorGateway>
}
