package com.example.readiumandroidtestapp.features.reader.ui.preferences

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.readiumandroidtestapp.R
import com.example.readiumandroidtestapp.features.reader.ui.state.ReaderPreferencesEditor
import com.example.readiumandroidtestapp.features.reader.ui.state.ReaderSettings
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
import org.readium.navigator.web.reflowable.preferences.ReflowableWebPreferencesEditor
import org.readium.r2.navigator.preferences.Preference
import org.readium.r2.navigator.preferences.PreferencesEditor

@RunWith(AndroidJUnit4::class)
class SettingsBottomSheetTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `displays ReflowableWeb settings`() {
        val editor = mockk<ReflowableWebPreferencesEditor>(relaxed = true)
        val settings = ReaderSettings.Configurable(ReaderPreferencesEditor.ReflowableWeb(editor))

        composeTestRule.setContent {
            SettingsBottomSheet(
                settings = settings,
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
        val settings = ReaderSettings.Configurable(ReaderPreferencesEditor.Pdf(editor))

        composeTestRule.setContent {
            SettingsBottomSheet(
                settings = settings,
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
        val settings = ReaderSettings.Tts(session)

        composeTestRule.setContent {
            SettingsBottomSheet(
                settings = settings,
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
        val settings = ReaderSettings.Configurable(ReaderPreferencesEditor.Audio(editor))

        composeTestRule.setContent {
            SettingsBottomSheet(
                settings = settings,
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
