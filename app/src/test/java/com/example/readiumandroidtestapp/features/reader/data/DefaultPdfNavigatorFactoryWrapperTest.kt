package com.example.readiumandroidtestapp.features.reader.data

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.readium.adapter.pdfium.navigator.PdfiumPreferences
import org.readium.adapter.pdfium.navigator.PdfiumPreferencesEditor
import org.readium.adapter.pdfium.navigator.PdfiumSettings
import org.readium.r2.navigator.pdf.PdfNavigatorFactory
import org.readium.r2.shared.publication.Publication

class DefaultPdfNavigatorFactoryWrapperTest {

    private val wrapper = DefaultPdfNavigatorFactoryWrapper()

    @Before
    fun setup() {
        mockkConstructor(PdfNavigatorFactory::class)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun createPreferencesEditor_createsFactoryAndEditor() {
        val publication = mockk<Publication>(relaxed = true)
        val initialPreferences = PdfiumPreferences()
        val editor = mockk<PdfiumPreferencesEditor>()

        every {
            anyConstructed<PdfNavigatorFactory<PdfiumSettings, PdfiumPreferences, PdfiumPreferencesEditor>>().createPreferencesEditor(
                initialPreferences = initialPreferences,
            )
        } returns editor

        val result = wrapper.createPreferencesEditor(
            publication = publication,
            initialPreferences = initialPreferences,
        )

        verify {
            anyConstructed<PdfNavigatorFactory<PdfiumSettings, PdfiumPreferences, PdfiumPreferencesEditor>>().createPreferencesEditor(
                initialPreferences = initialPreferences,
            )
        }
        assert(result == editor)
    }
}
