package com.example.readiumandroidtestapp.features.reader.data

import org.readium.adapter.pdfium.navigator.PdfiumDefaults
import org.readium.adapter.pdfium.navigator.PdfiumEngineProvider
import org.readium.adapter.pdfium.navigator.PdfiumPreferences
import org.readium.adapter.pdfium.navigator.PdfiumPreferencesEditor
import org.readium.r2.navigator.pdf.PdfNavigatorFactory
import org.readium.r2.shared.publication.Publication
import javax.inject.Inject

class DefaultPdfNavigatorFactoryWrapper @Inject constructor() : PdfNavigatorFactoryWrapper {
    override fun createPreferencesEditor(
        publication: Publication,
        initialPreferences: PdfiumPreferences,
    ): PdfiumPreferencesEditor {
        val factory = PdfNavigatorFactory(
            publication = publication,
            pdfEngineProvider = PdfiumEngineProvider(
                defaults = PdfiumDefaults(),
            ),
        )
        return factory.createPreferencesEditor(initialPreferences = initialPreferences)
    }
}
