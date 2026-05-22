package com.example.readiumandroidtestapp.features.reader.data

import org.readium.adapter.exoplayer.audio.ExoPlayerPreferencesSerializer
import org.readium.adapter.pdfium.navigator.PdfiumPreferencesSerializer
import org.readium.navigator.media.tts.android.AndroidTtsPreferencesSerializer

interface PreferencesSerializerFactory {
    fun createReflowableWebSerializer(): ReflowableWebPreferencesSerializer
    fun createFixedWebSerializer(): FixedWebPreferencesSerializer
    fun createPdfiumSerializer(): PdfiumPreferencesSerializer
    fun createAndroidTtsSerializer(): AndroidTtsPreferencesSerializer
    fun createExoPlayerSerializer(): ExoPlayerPreferencesSerializer
}
