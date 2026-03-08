package com.example.readiumandroidtestapp.features.reader.ui.preferences.sheets

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.filter
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.v2.createComposeRule
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
import io.mockk.spyk
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

    open class TestBooleanPreference : Preference<Boolean> {
        override val value: Boolean = false
        override val effectiveValue: Boolean = false
        override val isEffective: Boolean = true
        override fun set(value: Boolean?) {}
    }

    open class TestDoubleRangePreference : RangePreference<Double> {
        override val value: Double = 1.0
        override val effectiveValue: Double = 1.0
        override val isEffective: Boolean = true
        override val supportedRange: ClosedRange<Double> = 0.5..3.0
        override fun set(value: Double?) {}
        override fun increment() {}
        override fun decrement() {}
        override fun formatValue(value: Double): String = ""
    }

    open class TestSpreadPreference : EnumPreference<Spread> {
        override val value: Spread = Spread.AUTO
        override val effectiveValue: Spread = Spread.AUTO
        override val isEffective: Boolean = true
        override val supportedValues: List<Spread> = listOf(Spread.AUTO, Spread.NEVER)
        override fun set(value: Spread?) {}
    }

    open class TestThemePreference : EnumPreference<Theme> {
        override val value: Theme = Theme.LIGHT
        override val effectiveValue: Theme = Theme.LIGHT
        override val isEffective: Boolean = true
        override val supportedValues: List<Theme> = listOf(Theme.LIGHT, Theme.DARK)
        override fun set(value: Theme?) {}
    }

    open class TestFontFamilyPreference : Preference<FontFamily?> {
        override val value: FontFamily? = null
        override val effectiveValue: FontFamily? = null
        override val isEffective: Boolean = true
        override fun set(value: FontFamily?) {}
    }

    open class TestTextAlignPreference : EnumPreference<TextAlign?> {
        override val value: TextAlign? = TextAlign.START
        override val effectiveValue: TextAlign? = TextAlign.START
        override val isEffective: Boolean = true
        override val supportedValues: List<TextAlign?> = listOf(TextAlign.START, TextAlign.LEFT)
        override fun set(value: TextAlign?) {}
    }

    open class TestColumnCountPreference : EnumPreference<ColumnCount> {
        override val value: ColumnCount = ColumnCount.AUTO
        override val effectiveValue: ColumnCount = ColumnCount.AUTO
        override val isEffective: Boolean = true
        override val supportedValues: List<ColumnCount> = listOf(ColumnCount.AUTO, ColumnCount.ONE)
        override fun set(value: ColumnCount?) {}
    }

    @Test
    fun epubSettingsSheet_reflowableInteractions_allPreferencesEffective() {
        val editor = mockk<EpubPreferencesEditor>(relaxed = true)
        val onCommit = mockk<(Configurable.Preferences<*>) -> Unit>(relaxed = true)

        // Setup preferences
        val scrollPref = spyk(TestBooleanPreference())
        every { editor.scroll } returns scrollPref

        val spreadPref = spyk(TestSpreadPreference())
        every { editor.spread } returns spreadPref

        val themePref = spyk(TestThemePreference())
        every { editor.theme } returns themePref

        val fontSizePref = spyk(TestDoubleRangePreference())
        every { editor.fontSize } returns fontSizePref

        val fontFamilyPref = spyk(TestFontFamilyPreference())
        every { editor.fontFamily } returns fontFamilyPref

        val fontWeightPref = spyk(TestDoubleRangePreference())
        every { editor.fontWeight } returns fontWeightPref

        val lineHeightPref = spyk(TestDoubleRangePreference())
        every { editor.lineHeight } returns lineHeightPref

        val textAlignPref = spyk(TestTextAlignPreference())
        every { editor.textAlign } returns textAlignPref

        val hyphensPref = spyk(TestBooleanPreference())
        every { editor.hyphens } returns hyphensPref

        val ligaturesPref = spyk(TestBooleanPreference())
        every { editor.ligatures } returns ligaturesPref

        val columnCountPref = spyk(TestColumnCountPreference())
        every { editor.columnCount } returns columnCountPref

        val pageMarginsPref = spyk(TestDoubleRangePreference())
        every { editor.pageMargins } returns pageMarginsPref

        val paragraphIndentPref = spyk(TestDoubleRangePreference())
        every { editor.paragraphIndent } returns paragraphIndentPref

        val paragraphSpacingPref = spyk(TestDoubleRangePreference())
        every { editor.paragraphSpacing } returns paragraphSpacingPref

        val letterSpacingPref = spyk(TestDoubleRangePreference())
        every { editor.letterSpacing } returns letterSpacingPref

        val wordSpacingPref = spyk(TestDoubleRangePreference())
        every { editor.wordSpacing } returns wordSpacingPref

        val publisherStylesPref = spyk(TestBooleanPreference())
        every { editor.publisherStyles } returns publisherStylesPref

        composeTestRule.setContent {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                EpubSettingsSheet(
                    editor = editor,
                    isFixedLayout = false,
                    onCommit = onCommit,
                )
            }
        }

        // Scroll Mode
        composeTestRule.onNodeWithText("Scroll Mode").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("Scroll Mode").onParent().onChildren().filter(isToggleable())
            .onFirst().performClick()
        verify { scrollPref.set(any()) }
        verify(atLeast = 1) { onCommit(any()) }

        // Spread
        composeTestRule.onNodeWithText("Spread").performScrollTo().assertIsDisplayed()

        // Theme
        composeTestRule.onNodeWithText("Theme").performScrollTo().assertIsDisplayed()

        // Font Size
        composeTestRule.onNodeWithText("Font Size").performScrollTo().assertIsDisplayed()
        composeTestRule.onAllNodesWithContentDescription("Increase").onFirst().performClick()
        verify { fontSizePref.increment() }
        verify(atLeast = 1) { onCommit(any()) }
    }

    @Test
    fun epubSettingsSheet_fixedLayoutInteractions() {
        val editor = mockk<EpubPreferencesEditor>(relaxed = true)
        val onCommit = mockk<(Configurable.Preferences<*>) -> Unit>(relaxed = true)

        val spreadPref = spyk(TestSpreadPreference())
        every { editor.spread } returns spreadPref

        val themePref = spyk(TestThemePreference())
        every { editor.theme } returns themePref

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
