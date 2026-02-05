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
import org.readium.adapter.pdfium.navigator.PdfiumPreferencesEditor
import org.readium.r2.navigator.preferences.Axis
import org.readium.r2.navigator.preferences.Configurable
import org.readium.r2.navigator.preferences.EnumPreference
import org.readium.r2.navigator.preferences.Fit
import org.readium.r2.navigator.preferences.RangePreference

@RunWith(AndroidJUnit4::class)
class PdfSettingsSheetTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    abstract class TestFitPreference : EnumPreference<Fit>
    abstract class TestAxisPreference : EnumPreference<Axis>
    abstract class TestDoubleRangePreference : RangePreference<Double>

    @Test
    fun pdfSettingsSheet_fitInteraction() {
        val editor = mockk<PdfiumPreferencesEditor>(relaxed = true)
        val onCommit = mockk<(Configurable.Preferences<*>) -> Unit>(relaxed = true)

        val fitPref = mockk<TestFitPreference>(relaxed = true)
        every { editor.fit } returns fitPref
        every { fitPref.isEffective } returns true
        every { fitPref.value } returns Fit.CONTAIN
        every { fitPref.supportedValues } returns listOf(Fit.CONTAIN, Fit.WIDTH)

        composeTestRule.setContent {
            PdfSettingsSheet(
                editor = editor,
                onCommit = onCommit,
            )
        }

        composeTestRule.onNodeWithText(text = "Fit").assertExists()

        // Open dropdown (current value)
        composeTestRule.onNodeWithText(text = "Contain").performClick()

        // Select Width
        composeTestRule.onNodeWithText(text = "Fit Width").performClick()

        verify { fitPref.set(value = Fit.WIDTH) }
        verify { onCommit(any()) }
    }

    @Test
    fun pdfSettingsSheet_scrollAxisInteraction() {
        val editor = mockk<PdfiumPreferencesEditor>(relaxed = true)
        val onCommit = mockk<(Configurable.Preferences<*>) -> Unit>(relaxed = true)

        val axisPref = mockk<TestAxisPreference>(relaxed = true)
        every { editor.scrollAxis } returns axisPref
        every { axisPref.isEffective } returns true
        every { axisPref.value } returns Axis.VERTICAL
        every { axisPref.supportedValues } returns listOf(Axis.VERTICAL, Axis.HORIZONTAL)

        composeTestRule.setContent {
            PdfSettingsSheet(
                editor = editor,
                onCommit = onCommit,
            )
        }

        composeTestRule.onNodeWithText(text = "Scroll Axis").assertExists()

        // Open dropdown
        composeTestRule.onNodeWithText(text = "Vertical").performClick()

        // Select Horizontal
        composeTestRule.onNodeWithText(text = "Horizontal").performClick()

        verify { axisPref.set(value = Axis.HORIZONTAL) }
        verify { onCommit(any()) }
    }

    @Test
    fun pdfSettingsSheet_pageSpacingInteraction() {
        val editor = mockk<PdfiumPreferencesEditor>(relaxed = true)
        val onCommit = mockk<(Configurable.Preferences<*>) -> Unit>(relaxed = true)

        val spacingPref = mockk<TestDoubleRangePreference>(relaxed = true)
        every { editor.pageSpacing } returns spacingPref
        every { spacingPref.isEffective } returns true
        every { spacingPref.value } returns 1.0
        every { spacingPref.supportedRange } returns 0.0..50.0

        composeTestRule.setContent {
            PdfSettingsSheet(
                editor = editor,
                onCommit = onCommit,
            )
        }

        composeTestRule.onNodeWithText(text = "Page Spacing").assertExists()
        composeTestRule.onNodeWithContentDescription(label = "Increase").performClick()

        verify { spacingPref.increment() }
        verify { onCommit(any()) }
    }
}
