package com.example.readiumandroidtestapp.features.bookshelf.ui.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "w1080dp-h1920dp")
class BookshelfFabTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `BookshelfFab displays correct state`() {
        composeTestRule.setContent {
            BookshelfFab(
                expanded = false,
                onExpandedChange = {},
                onImportFromDevice = {},
                onImportFromStorage = {},
                onImportFromUrl = {},
            )
        }

        composeTestRule.onNodeWithContentDescription(label = "Toggle import menu")
            .assertIsDisplayed()
    }
}
