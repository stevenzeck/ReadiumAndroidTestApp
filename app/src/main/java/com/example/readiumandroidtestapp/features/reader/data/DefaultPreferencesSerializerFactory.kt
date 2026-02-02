package com.example.readiumandroidtestapp.features.reader.data

import org.readium.adapter.exoplayer.audio.ExoPlayerPreferencesSerializer
import org.readium.adapter.pdfium.navigator.PdfiumPreferencesSerializer
import org.readium.navigator.media.tts.android.AndroidTtsPreferencesSerializer
import org.readium.r2.navigator.epub.EpubPreferencesSerializer
import javax.inject.Inject

class DefaultPreferencesSerializerFactory @Inject constructor() : PreferencesSerializerFactory {
    override fun createEpubSerializer() = EpubPreferencesSerializer()
    override fun createPdfiumSerializer() = PdfiumPreferencesSerializer()
    override fun createAndroidTtsSerializer() = AndroidTtsPreferencesSerializer()
    override fun createExoPlayerSerializer() = ExoPlayerPreferencesSerializer()
}
