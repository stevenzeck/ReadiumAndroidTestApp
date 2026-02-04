package com.example.readiumandroidtestapp.features.reader.data

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.readium.r2.navigator.epub.EpubNavigatorFactory
import org.readium.r2.navigator.epub.EpubPreferences
import org.readium.r2.navigator.epub.EpubPreferencesEditor
import org.readium.r2.shared.publication.Publication

class DefaultEpubNavigatorFactoryWrapperTest {

    private val wrapper = DefaultEpubNavigatorFactoryWrapper()

    @Before
    fun setup() {
        mockkConstructor(EpubNavigatorFactory::class)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun createPreferencesEditor_createsFactoryAndEditor() {
        val publication = mockk<Publication>(relaxed = true)
        val initialPreferences = EpubPreferences()
        val editor = mockk<EpubPreferencesEditor>()

        every {
            anyConstructed<EpubNavigatorFactory>().createPreferencesEditor(currentPreferences = initialPreferences)
        } returns editor

        val result = wrapper.createPreferencesEditor(
            publication = publication,
            initialPreferences = initialPreferences,
        )

        verify {
            anyConstructed<EpubNavigatorFactory>().createPreferencesEditor(currentPreferences = initialPreferences)
        }
        assert(result == editor)
    }
}
