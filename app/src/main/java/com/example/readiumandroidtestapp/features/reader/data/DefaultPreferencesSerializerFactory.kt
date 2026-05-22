package com.example.readiumandroidtestapp.features.reader.data

import org.readium.adapter.exoplayer.audio.ExoPlayerPreferencesSerializer
import org.readium.adapter.pdfium.navigator.PdfiumPreferencesSerializer
import org.readium.navigator.media.tts.android.AndroidTtsPreferencesSerializer
import javax.inject.Inject

class DefaultPreferencesSerializerFactory @Inject constructor() : PreferencesSerializerFactory {
    override fun createReflowableWebSerializer() = ReflowableWebPreferencesSerializer()
    override fun createFixedWebSerializer() = FixedWebPreferencesSerializer()
    override fun createPdfiumSerializer() = PdfiumPreferencesSerializer()
    override fun createAndroidTtsSerializer() = AndroidTtsPreferencesSerializer()
    override fun createExoPlayerSerializer() = ExoPlayerPreferencesSerializer()
}
