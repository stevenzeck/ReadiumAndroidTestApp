package com.example.readiumandroidtestapp.features.reader.data

import org.readium.adapter.pdfium.navigator.PdfiumPreferences
import org.readium.r2.navigator.preferences.PreferencesEditor
import org.readium.r2.shared.publication.Publication

interface PdfNavigatorFactoryWrapper {
    fun createPreferencesEditor(
        publication: Publication,
        initialPreferences: PdfiumPreferences,
    ): PreferencesEditor<PdfiumPreferences>
}
