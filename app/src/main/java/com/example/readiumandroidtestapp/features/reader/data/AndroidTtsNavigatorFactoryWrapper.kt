package com.example.readiumandroidtestapp.features.reader.data

import android.app.Application
import org.readium.navigator.media.tts.AndroidTtsNavigator
import org.readium.navigator.media.tts.TtsNavigator
import org.readium.r2.shared.publication.Locator
import org.readium.r2.shared.publication.Publication

interface AndroidTtsNavigatorFactoryWrapper {
    suspend fun createNavigator(
        application: Application,
        publication: Publication,
        initialLocator: Locator,
        listener: TtsNavigator.Listener,
    ): Result<AndroidTtsNavigator>
}
