package com.example.readiumandroidtestapp.features.reader.ui.components

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.readiumandroidtestapp.R
import com.example.readiumandroidtestapp.features.reader.ui.state.ReaderActions
import com.example.readiumandroidtestapp.features.reader.ui.state.ReaderCapabilities
import io.mockk.mockk
import io.mockk.verify
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReaderOverlayTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val context: Context = ApplicationProvider.getApplicationContext()

    private val actions = mockk<ReaderActions>(relaxed = true)

    @Test
    fun readerOverlay_displaysTitleAndBackButton() {
        val title = "Book Title"
        composeTestRule.setContent {
            ReaderOverlay(
                visible = true,
                title = title,
                capabilities = ReaderCapabilities(
                    isSearchable = false,
                    canSpeak = false,
                    hasPreferences = false,
                ),
                isTtsActive = false,
                isPlaying = false,
                actions = actions,
            )
        }

        composeTestRule.onNodeWithText(text = title).assertIsDisplayed()

        val backDesc = context.getString(R.string.back)
        composeTestRule.onNodeWithContentDescription(label = backDesc).performClick()
        verify { actions.onNavigateBack() }
    }

    @Test
    fun readerOverlay_displaysSearchButton_whenSearchable() {
        composeTestRule.setContent {
            ReaderOverlay(
                visible = true,
                title = "Title",
                capabilities = ReaderCapabilities(
                    isSearchable = true,
                    canSpeak = false,
                    hasPreferences = false,
                ),
                isTtsActive = false,
                isPlaying = false,
                actions = actions,
            )
        }

        val searchDesc = context.getString(R.string.search)
        composeTestRule.onNodeWithContentDescription(label = searchDesc).performClick()
        verify { actions.onSearchClick() }
    }

    @Test
    fun readerOverlay_displaysTtsButton_whenCanSpeak() {
        composeTestRule.setContent {
            ReaderOverlay(
                visible = true,
                title = "Title",
                capabilities = ReaderCapabilities(
                    isSearchable = false,
                    canSpeak = true,
                    hasPreferences = false,
                ),
                isTtsActive = false,
                isPlaying = false,
                actions = actions,
            )
        }

        val ttsDesc = context.getString(R.string.tts)
        composeTestRule.onNodeWithContentDescription(label = ttsDesc).performClick()
        verify { actions.onTtsClick() }
    }

    @Test
    fun readerOverlay_displaysSettingsButton_whenHasPreferences() {
        composeTestRule.setContent {
            ReaderOverlay(
                visible = true,
                title = "Title",
                capabilities = ReaderCapabilities(
                    isSearchable = false,
                    canSpeak = false,
                    hasPreferences = true,
                ),
                isTtsActive = false,
                isPlaying = false,
                actions = actions,
            )
        }

        val settingsDesc = context.getString(R.string.reading_preferences)
        composeTestRule.onNodeWithContentDescription(label = settingsDesc).performClick()
        verify { actions.onSettingsClick() }
    }

    @Test
    fun readerOverlay_displaysTtsControls_whenTtsActive() {
        composeTestRule.setContent {
            ReaderOverlay(
                visible = true,
                title = "Title",
                capabilities = ReaderCapabilities(
                    isSearchable = true,
                    canSpeak = true,
                    hasPreferences = true,
                ),
                isTtsActive = true,
                isPlaying = false,
                actions = actions,
            )
        }

        val playDesc = context.getString(R.string.play)
        val prevDesc = context.getString(R.string.previous_sentence)
        val nextDesc = context.getString(R.string.next_sentence)
        val stopDesc = context.getString(R.string.stop)

        composeTestRule.onNodeWithContentDescription(label = playDesc).performClick()
        verify { actions.onTtsPlayPause() }

        composeTestRule.onNodeWithContentDescription(label = prevDesc).performClick()
        verify { actions.onTtsPrevious() }

        composeTestRule.onNodeWithContentDescription(label = nextDesc).performClick()
        verify { actions.onTtsNext() }

        composeTestRule.onNodeWithContentDescription(label = stopDesc).performClick()
        verify { actions.onTtsStop() }
    }
}
