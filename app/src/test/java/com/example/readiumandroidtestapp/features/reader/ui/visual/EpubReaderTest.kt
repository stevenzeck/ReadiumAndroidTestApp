package com.example.readiumandroidtestapp.features.reader.ui.visual

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.mockk
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.readium.r2.navigator.epub.EpubPreferences
import org.readium.r2.shared.publication.Publication

@RunWith(AndroidJUnit4::class)
class EpubReaderTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun epubReader_rendersWithoutCrash() {
        val publication = mockk<Publication>(relaxed = true)
        val preferences = EpubPreferences()

        composeTestRule.setContent {
            EpubReader(
                publication = publication,
                initialLocator = null,
                initialPreferences = preferences,
                onLocatorChanged = {},
                onTap = {},
                onNavigatorReady = {},
                onHighlight = {},
            )
        }
    }
}
