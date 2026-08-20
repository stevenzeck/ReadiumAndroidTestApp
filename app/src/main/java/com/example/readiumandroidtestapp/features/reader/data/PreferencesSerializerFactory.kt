package com.example.readiumandroidtestapp.features.reader.data

import org.readium.adapter.exoplayer.audio.ExoPlayerPreferencesSerializer
import org.readium.adapter.pdfium.navigator.PdfiumPreferencesSerializer
import org.readium.navigator.media.tts.android.AndroidTtsPreferencesSerializer
import org.readium.r2.navigator.epub.EpubPreferencesSerializer
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesSerializerFactory @Inject constructor() {
    fun createEpubSerializer() = EpubPreferencesSerializer()
    fun createPdfiumSerializer() = PdfiumPreferencesSerializer()
    fun createAndroidTtsSerializer() = AndroidTtsPreferencesSerializer()
    fun createExoPlayerSerializer() = ExoPlayerPreferencesSerializer()
}
