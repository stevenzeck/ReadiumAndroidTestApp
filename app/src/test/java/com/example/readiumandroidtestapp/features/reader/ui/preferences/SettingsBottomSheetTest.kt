package com.example.readiumandroidtestapp.features.reader.ui.preferences

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.readiumandroidtestapp.R
import com.example.readiumandroidtestapp.features.reader.ui.state.TtsSettingsSession
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.readium.adapter.exoplayer.audio.ExoPlayerPreferences
import org.readium.adapter.pdfium.navigator.PdfiumPreferencesEditor
import org.readium.navigator.media.tts.android.AndroidTtsEngine
import org.readium.navigator.media.tts.android.AndroidTtsPreferencesEditor
import org.readium.r2.navigator.epub.EpubPreferencesEditor
import org.readium.r2.navigator.preferences.Preference
import org.readium.r2.navigator.preferences.PreferencesEditor

@RunWith(AndroidJUnit4::class)
class SettingsBottomSheetTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `displays Epub settings`() {
        val editor = mockk<EpubPreferencesEditor>(relaxed = true)

        composeTestRule.setContent {
            SettingsBottomSheet(
                settings = editor,
                isFixedLayout = false,
                onCommit = {},
                onDismissRequest = {},
            )
        }

        val context = ApplicationProvider.getApplicationContext<Context>()
        val title = context.getString(R.string.reading_preferences)
        val reset = context.getString(R.string.reset_to_defaults)

        composeTestRule.onNodeWithText(text = title).assertIsDisplayed()
        composeTestRule.onNodeWithText(text = reset).assertIsDisplayed()
    }

    @Test
    fun `displays Pdf settings`() {
        val editor = mockk<PdfiumPreferencesEditor>(relaxed = true)

        composeTestRule.setContent {
            SettingsBottomSheet(
                settings = editor,
                isFixedLayout = false,
                onCommit = {},
                onDismissRequest = {},
            )
        }

        val context = ApplicationProvider.getApplicationContext<Context>()
        val title = context.getString(R.string.reading_preferences)

        composeTestRule.onNodeWithText(text = title).assertIsDisplayed()
    }

    @Test
    fun `displays Tts settings and handles reset`() {
        val editor = mockk<AndroidTtsPreferencesEditor>(relaxed = true)
        val voicePreference = mockk<Preference<AndroidTtsEngine.Voice?>>(relaxed = true)
        val session = TtsSettingsSession(
            editor = editor,
            voice = voicePreference,
            availableLanguages = emptyList(),
            availableVoices = emptyList(),
        )

        composeTestRule.setContent {
            SettingsBottomSheet(
                settings = session,
                isFixedLayout = false,
                onCommit = {},
                onDismissRequest = {},
            )
        }

        val context = ApplicationProvider.getApplicationContext<Context>()
        val title = context.getString(R.string.reading_preferences)
        val reset = context.getString(R.string.reset_to_defaults)

        composeTestRule.onNodeWithText(text = title).assertIsDisplayed()

        composeTestRule.onNodeWithText(text = reset).performClick()
        verify { editor.clear() }
    }

    @Test
    fun `displays Audiobook settings and handles reset`() {
        val editor = mockk<PreferencesEditor<ExoPlayerPreferences>>(relaxed = true)
        val preferences = mockk<ExoPlayerPreferences>(relaxed = true)
        every { editor.preferences } returns preferences

        composeTestRule.setContent {
            SettingsBottomSheet(
                settings = editor,
                isFixedLayout = false,
                onCommit = {},
                onDismissRequest = {},
            )
        }

        val context = ApplicationProvider.getApplicationContext<Context>()
        val title = context.getString(R.string.reading_preferences)
        val reset = context.getString(R.string.reset_to_defaults)

        composeTestRule.onNodeWithText(text = title).assertIsDisplayed()

        composeTestRule.onNodeWithText(text = reset).performClick()
        verify { editor.clear() }
    }
}
