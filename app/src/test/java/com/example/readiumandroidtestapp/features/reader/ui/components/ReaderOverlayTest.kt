package com.example.readiumandroidtestapp.features.reader.ui.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.readiumandroidtestapp.features.reader.ui.state.ReaderActions
import com.example.readiumandroidtestapp.features.reader.ui.state.ReaderCapabilities
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ReaderOverlayTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val capabilities = ReaderCapabilities(
        isSearchable = true,
        canSpeak = true,
        hasPreferences = true,
    )

    private val actions = ReaderActions(
        onNavigateBack = {},
        onSearchClick = {},
        onTtsClick = {},
        onSettingsClick = {},
        onTocClick = {},
        onTtsPlayPause = {},
        onTtsPrevious = {},
        onTtsNext = {},
        onTtsStop = {},
    )

    @Test
    fun readerOverlay_displaysTitle() {
        composeTestRule.setContent {
            ReaderOverlay(
                visible = true,
                title = "My Book",
                capabilities = capabilities,
                isTtsActive = false,
                isPlaying = false,
                actions = actions,
            )
        }

        composeTestRule.onNodeWithText(text = "My Book").assertIsDisplayed()
    }

    @Test
    fun readerOverlay_handlesSearchClick() {
        var clicked = false
        val testActions = actions.copy(onSearchClick = { clicked = true })

        composeTestRule.setContent {
            ReaderOverlay(
                visible = true,
                title = "My Book",
                capabilities = capabilities,
                isTtsActive = false,
                isPlaying = false,
                actions = testActions,
            )
        }

        val searchString =
            androidx.test.core.app.ApplicationProvider.getApplicationContext<android.content.Context>()
                .getString(com.example.readiumandroidtestapp.R.string.search)

        composeTestRule.onNodeWithContentDescription(label = searchString).performClick()
        assertTrue(clicked)
    }
}
