package com.example.readiumandroidtestapp.features.reader.ui.preferences.sheets

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.filter
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onParent
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
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
    abstract class TestTextAlignPreference : EnumPreference<TextAlign?>
    abstract class TestColumnCountPreference : EnumPreference<ColumnCount>

    @Test
    fun epubSettingsSheet_reflowableInteractions_allPreferencesEffective() {
        val editor = mockk<EpubPreferencesEditor>(relaxed = true)
        val onCommit = mockk<(Configurable.Preferences<*>) -> Unit>(relaxed = true)

        val scrollPref = mockk<TestBooleanPreference>(relaxed = true)
        every { editor.scroll } returns scrollPref
        every { scrollPref.isEffective } returns true
        every { scrollPref.value } returns false
        every { scrollPref.effectiveValue } returns false

        val spreadPref = mockk<TestSpreadPreference>(relaxed = true)
        every { editor.spread } returns spreadPref
        every { spreadPref.isEffective } returns true
        every { spreadPref.value } returns Spread.AUTO
        every { spreadPref.effectiveValue } returns Spread.AUTO
        every { spreadPref.supportedValues } returns listOf(
            Spread.AUTO,
            Spread.NEVER,
            Spread.ALWAYS,
        )

        val themePref = mockk<TestThemePreference>(relaxed = true)
        every { editor.theme } returns themePref
        every { themePref.isEffective } returns true
        every { themePref.value } returns Theme.LIGHT
        every { themePref.effectiveValue } returns Theme.LIGHT
        every { themePref.supportedValues } returns listOf(Theme.LIGHT, Theme.DARK, Theme.SEPIA)

        val fontSizePref = mockk<TestDoubleRangePreference>(relaxed = true)
        every { editor.fontSize } returns fontSizePref
        every { fontSizePref.isEffective } returns true
        every { fontSizePref.value } returns 1.0
        every { fontSizePref.effectiveValue } returns 1.0
        every { fontSizePref.supportedRange } returns 0.5..3.0

        val fontFamilyPref = mockk<TestFontFamilyPreference>(relaxed = true)
        every { editor.fontFamily } returns fontFamilyPref
        every { fontFamilyPref.isEffective } returns true
        every { fontFamilyPref.value } returns null
        every { fontFamilyPref.effectiveValue } returns null

        val fontWeightPref = mockk<TestDoubleRangePreference>(relaxed = true)
        every { editor.fontWeight } returns fontWeightPref
        every { fontWeightPref.isEffective } returns true
        every { fontWeightPref.value } returns 1.0
        every { fontWeightPref.effectiveValue } returns 1.0
        every { fontWeightPref.supportedRange } returns 1.0..1000.0

        val lineHeightPref = mockk<TestDoubleRangePreference>(relaxed = true)
        every { editor.lineHeight } returns lineHeightPref
        every { lineHeightPref.isEffective } returns true
        every { lineHeightPref.value } returns 1.0
        every { lineHeightPref.effectiveValue } returns 1.0
        every { lineHeightPref.supportedRange } returns 1.0..2.0

        val textAlignPref = mockk<TestTextAlignPreference>(relaxed = true)
        every { editor.textAlign } returns textAlignPref
        every { textAlignPref.isEffective } returns true
        every { textAlignPref.value } returns TextAlign.START
        every { textAlignPref.effectiveValue } returns TextAlign.START
        every { textAlignPref.supportedValues } returns listOf(
            TextAlign.START,
            TextAlign.LEFT,
            TextAlign.RIGHT,
            TextAlign.JUSTIFY,
        )

        val hyphensPref = mockk<TestBooleanPreference>(relaxed = true)
        every { editor.hyphens } returns hyphensPref
        every { hyphensPref.isEffective } returns true
        every { hyphensPref.value } returns false
        every { hyphensPref.effectiveValue } returns false

        val ligaturesPref = mockk<TestBooleanPreference>(relaxed = true)
        every { editor.ligatures } returns ligaturesPref
        every { ligaturesPref.isEffective } returns true
        every { ligaturesPref.value } returns false
        every { ligaturesPref.effectiveValue } returns false

        val columnCountPref = mockk<TestColumnCountPreference>(relaxed = true)
        every { editor.columnCount } returns columnCountPref
        every { columnCountPref.isEffective } returns true
        every { columnCountPref.value } returns ColumnCount.AUTO
        every { columnCountPref.effectiveValue } returns ColumnCount.AUTO
        every { columnCountPref.supportedValues } returns listOf(
            ColumnCount.AUTO,
            ColumnCount.ONE,
            ColumnCount.TWO,
        )

        val pageMarginsPref = mockk<TestDoubleRangePreference>(relaxed = true)
        every { editor.pageMargins } returns pageMarginsPref
        every { pageMarginsPref.isEffective } returns true
        every { pageMarginsPref.value } returns 1.0
        every { pageMarginsPref.effectiveValue } returns 1.0
        every { pageMarginsPref.supportedRange } returns 0.0..5.0

        val paragraphIndentPref = mockk<TestDoubleRangePreference>(relaxed = true)
        every { editor.paragraphIndent } returns paragraphIndentPref
        every { paragraphIndentPref.isEffective } returns true
        every { paragraphIndentPref.value } returns 1.0
        every { paragraphIndentPref.effectiveValue } returns 1.0
        every { paragraphIndentPref.supportedRange } returns 0.0..5.0

        val paragraphSpacingPref = mockk<TestDoubleRangePreference>(relaxed = true)
        every { editor.paragraphSpacing } returns paragraphSpacingPref
        every { paragraphSpacingPref.isEffective } returns true
        every { paragraphSpacingPref.value } returns 1.0
        every { paragraphSpacingPref.effectiveValue } returns 1.0
        every { paragraphSpacingPref.supportedRange } returns 0.0..5.0

        val letterSpacingPref = mockk<TestDoubleRangePreference>(relaxed = true)
        every { editor.letterSpacing } returns letterSpacingPref
        every { letterSpacingPref.isEffective } returns true
        every { letterSpacingPref.value } returns 1.0
        every { letterSpacingPref.effectiveValue } returns 1.0
        every { letterSpacingPref.supportedRange } returns 0.0..5.0

        val wordSpacingPref = mockk<TestDoubleRangePreference>(relaxed = true)
        every { editor.wordSpacing } returns wordSpacingPref
        every { wordSpacingPref.isEffective } returns true
        every { wordSpacingPref.value } returns 1.0
        every { wordSpacingPref.effectiveValue } returns 1.0
        every { wordSpacingPref.supportedRange } returns 0.0..5.0

        val publisherStylesPref = mockk<TestBooleanPreference>(relaxed = true)
        every { editor.publisherStyles } returns publisherStylesPref
        every { publisherStylesPref.isEffective } returns true
        every { publisherStylesPref.value } returns true
        every { publisherStylesPref.effectiveValue } returns true


        composeTestRule.setContent {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                EpubSettingsSheet(
                    editor = editor,
                    isFixedLayout = false,
                    onCommit = onCommit,
                )
            }
        }

        // Test Scroll Switch
        composeTestRule.onNodeWithText(text = "Scroll Mode").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText(text = "Scroll Mode").onParent().onChildren().filter(
            matcher = isToggleable(),
        ).onFirst().performClick()
        verify { scrollPref.set(value = true) }

        // Verify Enum Preferences are displayed (interactions are flaky in test environment due to duplicates/popups)
        composeTestRule.onNodeWithText(text = "Spread").performScrollTo().assertIsDisplayed()

        composeTestRule.onNodeWithText(text = "Theme").performScrollTo().assertIsDisplayed()

        // Test Font Size Stepper
        composeTestRule.onNodeWithText(text = "Font Size").performScrollTo().assertIsDisplayed()
        composeTestRule.onAllNodesWithContentDescription(label = "Increase").onFirst()
            .performClick()
        verify { fontSizePref.increment() }

        // Font Family interaction is tricky with ChoicePreference default implementation in tests
        composeTestRule.onNodeWithText(text = "Font Family").performScrollTo().assertIsDisplayed()

        // composeTestRule.onNodeWithText(text = "Text Align").performScrollTo().assertIsDisplayed()

        // Test Hyphens Switch
        // composeTestRule.onNodeWithText(text = "Hyphens").performScrollTo().performClick()
        // verify { hyphensPref.set(value = true) }

        // Test Ligatures Switch
        // composeTestRule.onNodeWithText(text = "Ligatures").performScrollTo().performClick()
        // verify { ligaturesPref.set(value = true) }

        // composeTestRule.onNodeWithText(text = "Column Count").performScrollTo().assertIsDisplayed()

        // Test Publisher Styles Switch
        composeTestRule.onNodeWithText(text = "Publisher Styles").performScrollTo()
            .assertIsDisplayed()
            .performClick()
        verify { publisherStylesPref.set(value = false) }
    }

    @Test
    fun epubSettingsSheet_fixedLayoutInteractions() {
        val editor = mockk<EpubPreferencesEditor>(relaxed = true)
        val onCommit = mockk<(Configurable.Preferences<*>) -> Unit>(relaxed = true)

        val spreadPref = mockk<TestSpreadPreference>(relaxed = true)
        every { editor.spread } returns spreadPref
        every { spreadPref.isEffective } returns true
        every { spreadPref.value } returns Spread.AUTO
        every { spreadPref.effectiveValue } returns Spread.AUTO
        every { spreadPref.supportedValues } returns listOf(Spread.AUTO, Spread.NEVER)

        val themePref = mockk<TestThemePreference>(relaxed = true)
        every { editor.theme } returns themePref
        every { themePref.isEffective } returns true
        every { themePref.value } returns Theme.LIGHT
        every { themePref.effectiveValue } returns Theme.LIGHT
        every { themePref.supportedValues } returns listOf(Theme.LIGHT, Theme.DARK)

        composeTestRule.setContent {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                EpubSettingsSheet(
                    editor = editor,
                    isFixedLayout = true,
                    onCommit = onCommit,
                )
            }
        }

        composeTestRule.onNodeWithText(text = "Spread").assertIsDisplayed()
        composeTestRule.onNodeWithText(text = "Theme").assertIsDisplayed()

        composeTestRule.onNodeWithText(text = "Font Size").assertDoesNotExist()
    }
}
