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
import org.readium.r2.navigator.epub.EpubPreferencesEditor
import org.readium.r2.navigator.preferences.ColumnCount
import org.readium.r2.navigator.preferences.Configurable
import org.readium.r2.navigator.preferences.EnumPreference
import org.readium.r2.navigator.preferences.FontFamily
import org.readium.r2.navigator.preferences.Preference
import org.readium.r2.navigator.preferences.RangePreference
import org.readium.r2.navigator.preferences.Spread
import org.readium.r2.navigator.preferences.TextAlign
import org.readium.r2.navigator.preferences.Theme

@RunWith(AndroidJUnit4::class)
class EpubSettingsSheetTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    abstract class TestBooleanPreference : Preference<Boolean>
    abstract class TestDoubleRangePreference : RangePreference<Double>
    abstract class TestSpreadPreference : EnumPreference<Spread>
    abstract class TestThemePreference : EnumPreference<Theme>
    abstract class TestFontFamilyPreference : Preference<FontFamily?>
    abstract class TestTextAlignPreference : EnumPreference<TextAlign>
    abstract class TestColumnCountPreference : EnumPreference<ColumnCount>

    @Test
    fun epubSettingsSheet_reflowableInteractions() {
        val editor = mockk<EpubPreferencesEditor>(relaxed = true)
        val onCommit = mockk<(Configurable.Preferences<*>) -> Unit>(relaxed = true)

        val scrollPref = mockk<TestBooleanPreference>(relaxed = true)
        every { editor.scroll } returns scrollPref
        every { scrollPref.isEffective } returns true
        every { scrollPref.value } returns false

        val fontSizePref = mockk<TestDoubleRangePreference>(relaxed = true)
        every { editor.fontSize } returns fontSizePref
        every { fontSizePref.isEffective } returns true
        every { fontSizePref.value } returns 1.0
        every { fontSizePref.supportedRange } returns 0.5..3.0

        composeTestRule.setContent {
            EpubSettingsSheet(
                editor = editor,
                isFixedLayout = false,
                onCommit = onCommit,
            )
        }

        composeTestRule.onNodeWithContentDescription(label = "Increase").performClick()
        verify { fontSizePref.increment() }
        verify { onCommit(any()) }
    }

    @Test
    fun epubSettingsSheet_fixedLayoutInteractions() {
        val editor = mockk<EpubPreferencesEditor>(relaxed = true)
        val onCommit = mockk<(Configurable.Preferences<*>) -> Unit>(relaxed = true)

        val spreadPref = mockk<TestSpreadPreference>(relaxed = true)
        every { editor.spread } returns spreadPref
        every { spreadPref.isEffective } returns true
        every { spreadPref.value } returns Spread.AUTO

        composeTestRule.setContent {
            EpubSettingsSheet(
                editor = editor,
                isFixedLayout = true,
                onCommit = onCommit,
            )
        }

        composeTestRule.onNodeWithText(text = "Spread").assertExists()
    }
}
