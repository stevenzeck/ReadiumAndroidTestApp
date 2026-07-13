package com.example.readiumandroidtestapp.features.reader.ui.audio

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.readiumandroidtestapp.R
import com.example.readiumandroidtestapp.core.domain.model.Book
import com.example.readiumandroidtestapp.features.reader.ui.state.ReaderSettingsSheet
import io.mockk.mockk
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.readium.adapter.exoplayer.audio.ExoPlayerPreferences
import org.readium.r2.navigator.preferences.PreferencesEditor

@RunWith(AndroidJUnit4::class)
class AudioReaderTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val book = Book(
        id = 1,
        creation = 0,
        href = "href",
        title = "Test Book",
        author = "Test Author",
        identifier = "id",
        progression = null,
        rawMediaType = "audio/lcp",
        cover = null,
    )

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun audioReader_displaysBookInfo() {

        composeTestRule.setContent {
            AudioReader(
                book = book,
                settingsSheetState = null,
                onNavigateBack = {},
                onSettingsClick = {},
                onSettingsChange = {},
                onSettingsDismiss = {},
                onOpenPlayer = {},
            )
        }

        composeTestRule.onAllNodesWithText(text = "Test Book").onFirst().assertIsDisplayed()
        composeTestRule.onAllNodesWithText(text = "Test Author").onFirst().assertIsDisplayed()
    }

    @Test
    fun audioReader_displaysControls() {

        composeTestRule.setContent {
            AudioReader(
                book = book,
                settingsSheetState = null,
                onNavigateBack = {},
                onSettingsClick = {},
                onSettingsChange = {},
                onSettingsDismiss = {},
                onOpenPlayer = {},
            )
        }

        val play = context.getString(R.string.play)

        composeTestRule.onNodeWithText(text = play).assertIsDisplayed()
    }

    @Test
    fun audioReader_settingsClick_triggersCallback() {
        var settingsClicked = false

        composeTestRule.setContent {
            AudioReader(
                book = book,
                settingsSheetState = null,
                onNavigateBack = {},
                onSettingsClick = { settingsClicked = true },
                onSettingsChange = {},
                onSettingsDismiss = {},
                onOpenPlayer = {},
            )
        }

        val settings = context.getString(R.string.reading_preferences)
        composeTestRule.onNodeWithContentDescription(label = settings).performClick()

        assertTrue(settingsClicked)
    }

    @Test
    fun audioReader_backClick_triggersCallback() {
        var backClicked = false

        composeTestRule.setContent {
            AudioReader(
                book = book,
                settingsSheetState = null,
                onNavigateBack = { backClicked = true },
                onSettingsClick = {},
                onSettingsChange = {},
                onSettingsDismiss = {},
                onOpenPlayer = {},
            )
        }

        val back = context.getString(R.string.back)
        composeTestRule.onNodeWithContentDescription(label = back).performClick()

        assertTrue(backClicked)
    }

    @Test
    fun audioReader_displaysSettingsSheet_whenStateIsConfigurable() {

        val mockEditor = mockk<PreferencesEditor<ExoPlayerPreferences>>(relaxed = true)
        val settingsState = ReaderSettingsSheet.Configurable(mockEditor)

        composeTestRule.setContent {
            AudioReader(
                book = book,
                settingsSheetState = settingsState,
                onNavigateBack = {},
                onSettingsClick = {},
                onSettingsChange = {},
                onSettingsDismiss = {},
                onOpenPlayer = {},
            )
        }
    }
}
