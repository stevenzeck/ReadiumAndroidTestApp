package com.example.readiumandroidtestapp.features.reader.ui.preferences.sheets

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.readiumandroidtestapp.features.reader.ui.state.TtsSettingsSession
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.readium.navigator.media.tts.android.AndroidTtsEngine
import org.readium.navigator.media.tts.android.AndroidTtsPreferencesEditor
import org.readium.r2.navigator.preferences.Configurable
import org.readium.r2.navigator.preferences.Preference
import org.readium.r2.navigator.preferences.RangePreference
import java.util.Locale

@RunWith(AndroidJUnit4::class)
class TtsSettingsSheetTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    abstract class TestDoubleRangePreference : RangePreference<Double>
    abstract class TestVoicePreference : Preference<AndroidTtsEngine.Voice?>
    abstract class TestLanguagePreference : Preference<org.readium.r2.shared.util.Language?>

    @Test
    fun ttsSettingsSheet_speedInteraction() {
        val editor = mockk<AndroidTtsPreferencesEditor>(relaxed = true)
        val session = mockk<TtsSettingsSession>(relaxed = true)
        val onCommit = mockk<(Configurable.Preferences<*>) -> Unit>(relaxed = true)

        every { session.editor } returns editor

        val speedPref = mockk<TestDoubleRangePreference>(relaxed = true)
        every { editor.speed } returns speedPref
        every { speedPref.isEffective } returns true
        every { speedPref.value } returns 1.0
        every { speedPref.supportedRange } returns 0.5..3.0

        composeTestRule.setContent {
            TtsSettingsSheet(
                session = session,
                onCommit = onCommit,
            )
        }

        composeTestRule.onNodeWithText(text = "Speed").assertExists()
        composeTestRule.onNodeWithContentDescription(label = "Increase").performClick()
        verify { speedPref.increment() }
        verify { onCommit(any()) }
    }

    @Test
    fun ttsSettingsSheet_voiceInteraction() {
        val editor = mockk<AndroidTtsPreferencesEditor>(relaxed = true)
        val session = mockk<TtsSettingsSession>(relaxed = true)
        val onCommit = mockk<(Configurable.Preferences<*>) -> Unit>(relaxed = true)

        every { session.editor } returns editor

        val voicePref = mockk<TestVoicePreference>(relaxed = true)
        every { session.voice } returns voicePref
        every { voicePref.isEffective } returns true

        val voice = mockk<AndroidTtsEngine.Voice>()
        every { voice.language } returns org.readium.r2.shared.util.Language(Locale.ENGLISH)
        every { voicePref.value } returns voice
        every { session.availableVoices } returns listOf(voice)

        composeTestRule.setContent {
            TtsSettingsSheet(
                session = session,
                onCommit = onCommit,
            )
        }

        composeTestRule.onNodeWithText(text = "Voice").assertExists()
        composeTestRule.onAllNodesWithText(text = "English").onFirst().performClick()
        composeTestRule.onAllNodesWithText(text = "English")[1].performClick()

        verify { voicePref.set(value = voice) }
        verify { onCommit(any()) }
    }

    @Test
    fun ttsSettingsSheet_pitchInteraction() {
        val editor = mockk<AndroidTtsPreferencesEditor>(relaxed = true)
        val session = mockk<TtsSettingsSession>(relaxed = true)
        val onCommit = mockk<(Configurable.Preferences<*>) -> Unit>(relaxed = true)

        every { session.editor } returns editor

        val pitchPref = mockk<TestDoubleRangePreference>(relaxed = true)
        every { editor.pitch } returns pitchPref
        every { pitchPref.isEffective } returns true
        every { pitchPref.value } returns 1.0
        every { pitchPref.supportedRange } returns 0.5..2.0

        composeTestRule.setContent {
            TtsSettingsSheet(
                session = session,
                onCommit = onCommit,
            )
        }

        composeTestRule.onNodeWithText(text = "Pitch").assertExists()
        composeTestRule.onNodeWithContentDescription(label = "Increase").performClick()
        verify { pitchPref.increment() }
        verify { onCommit(any()) }
    }

    @Test
    fun ttsSettingsSheet_languageInteraction() {
        val editor = mockk<AndroidTtsPreferencesEditor>(relaxed = true)
        val session = mockk<TtsSettingsSession>(relaxed = true)
        val onCommit = mockk<(Configurable.Preferences<*>) -> Unit>(relaxed = true)

        every { session.editor } returns editor

        val languagePref = mockk<TestLanguagePreference>(relaxed = true)
        every { editor.language } returns languagePref
        every { languagePref.isEffective } returns true

        val english = org.readium.r2.shared.util.Language(Locale.ENGLISH)
        every { languagePref.value } returns english
        every { session.availableLanguages } returns listOf(english)

        composeTestRule.setContent {
            TtsSettingsSheet(
                session = session,
                onCommit = onCommit,
            )
        }

        composeTestRule.onNodeWithText(text = "Language").assertExists()
        composeTestRule.onAllNodesWithText(text = "English").onFirst().performClick()
        composeTestRule.onAllNodesWithText(text = "English")[1].performClick()

        verify { languagePref.set(value = english) }
        verify { onCommit(any()) }
    }
}
