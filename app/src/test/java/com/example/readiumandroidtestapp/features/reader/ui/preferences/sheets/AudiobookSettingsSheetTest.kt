package com.example.readiumandroidtestapp.features.reader.ui.preferences.sheets

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.readium.adapter.exoplayer.audio.ExoPlayerPreferencesEditor
import org.readium.r2.navigator.preferences.Configurable
import org.readium.r2.navigator.preferences.RangePreference

@RunWith(AndroidJUnit4::class)
class AudiobookSettingsSheetTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    abstract class TestDoubleRangePreference : RangePreference<Double>

    @Test
    fun audiobookSettingsSheet_speedInteraction() {
        val editor = mockk<ExoPlayerPreferencesEditor>(relaxed = true)
        val onCommit = mockk<(Configurable.Preferences<*>) -> Unit>(relaxed = true)

        val speedPref = mockk<TestDoubleRangePreference>(relaxed = true)
        every { editor.speed } returns speedPref
        every { speedPref.isEffective } returns true
        every { speedPref.value } returns 1.0
        every { speedPref.supportedRange } returns 0.5..3.0

        composeTestRule.setContent {
            AudiobookSettingsSheet(
                editor = editor,
                onCommit = onCommit,
            )
        }

        composeTestRule.onNodeWithText(text = "Speed").assertExists()
        composeTestRule.onNodeWithContentDescription(label = "Decrease").performClick()
        verify { speedPref.decrement() }
        verify { onCommit(any()) }
    }

    @Test
    fun audiobookSettingsSheet_pitchInteraction() {
        val editor = mockk<ExoPlayerPreferencesEditor>(relaxed = true)
        val onCommit = mockk<(Configurable.Preferences<*>) -> Unit>(relaxed = true)

        val pitchPref = mockk<TestDoubleRangePreference>(relaxed = true)
        every { editor.pitch } returns pitchPref
        every { pitchPref.isEffective } returns true
        every { pitchPref.value } returns 1.0
        every { pitchPref.supportedRange } returns 0.5..3.0

        composeTestRule.setContent {
            AudiobookSettingsSheet(
                editor = editor,
                onCommit = onCommit,
            )
        }

        composeTestRule.onNodeWithText(text = "Pitch").assertExists()
        composeTestRule.onNodeWithContentDescription(label = "Increase").performClick()
        verify { pitchPref.increment() }
        verify { onCommit(any()) }
    }
}
