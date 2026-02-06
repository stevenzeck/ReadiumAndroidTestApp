package com.example.readiumandroidtestapp.features.reader.data

import android.app.Application
import org.readium.navigator.media.tts.AndroidTtsNavigatorFactory
import org.readium.r2.shared.publication.Publication
import javax.inject.Inject

interface AndroidTtsNavigatorFactoryProvider {
    suspend fun create(
        application: Application,
        publication: Publication,
    ): AndroidTtsNavigatorFactory?
}

class DefaultAndroidTtsNavigatorFactoryProvider @Inject constructor() :
    AndroidTtsNavigatorFactoryProvider {
    override suspend fun create(
        application: Application,
        publication: Publication,
    ): AndroidTtsNavigatorFactory? {
        return AndroidTtsNavigatorFactory(application = application, publication = publication)
    }
}
