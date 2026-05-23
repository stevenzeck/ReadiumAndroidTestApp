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
import org.readium.navigator.web.fixedlayout.preferences.FixedWebPreferencesEditor
import org.readium.navigator.web.reflowable.preferences.ReflowableWebPreferencesEditor
import org.readium.r2.navigator.preferences.FontFamily
import org.readium.r2.navigator.preferences.Preference
import org.readium.r2.navigator.preferences.RangePreference

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

    open class TestFontFamilyPreference : Preference<FontFamily?> {
        override val value: FontFamily? = null
        override val effectiveValue: FontFamily? = null
        override val isEffective: Boolean = true
        override fun set(value: FontFamily?) {}
    }

    @Test
    fun reflowableWebSettingsSheet_interactions() {
        val editor = mockk<ReflowableWebPreferencesEditor>(relaxed = true)
        val onCommit = mockk<() -> Unit>(relaxed = true)

        // Setup preferences
        val scrollPref = spyk(TestBooleanPreference())
        every { editor.scroll } returns scrollPref

        val fontSizePref = spyk(TestDoubleRangePreference())
        every { editor.fontSize } returns fontSizePref

        val fontFamilyPref = spyk(TestFontFamilyPreference())
        every { editor.fontFamily } returns fontFamilyPref

        composeTestRule.setContent {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                ReflowableWebSettingsSheet(
                    editor = editor,
                    onCommit = onCommit,
                )
            }
        }

        // Scroll Mode
        composeTestRule.onNodeWithText("Scroll Mode").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("Scroll Mode").onParent().onChildren().filter(isToggleable())
            .onFirst().performClick()
        verify { scrollPref.set(any()) }
        verify(atLeast = 1) { onCommit() }

        // Font Size
        composeTestRule.onNodeWithText("Font Size").performScrollTo().assertIsDisplayed()
        composeTestRule.onAllNodesWithContentDescription("Increase").onFirst().performClick()
        verify { fontSizePref.increment() }
        verify(atLeast = 1) { onCommit() }
    }

    @Test
    fun fixedWebSettingsSheet_interactions() {
        val editor = mockk<FixedWebPreferencesEditor>(relaxed = true)
        val onCommit = mockk<() -> Unit>(relaxed = true)

        val spreadsPref = spyk(TestBooleanPreference())
        every { editor.spreads } returns spreadsPref

        composeTestRule.setContent {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                FixedWebSettingsSheet(
                    editor = editor,
                    onCommit = onCommit,
                )
            }
        }

        composeTestRule.onNodeWithText(text = "Spread").assertIsDisplayed()
        composeTestRule.onNodeWithText(text = "Spread").onParent().onChildren()
            .filter(isToggleable())
            .onFirst().performClick()
        verify { spreadsPref.set(any()) }
        verify(atLeast = 1) { onCommit() }
    }
}
