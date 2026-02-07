package com.example.readiumandroidtestapp.features.reader.ui.preferences

import android.content.res.Resources
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.readiumandroidtestapp.R
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.readium.r2.navigator.preferences.Axis
import org.readium.r2.navigator.preferences.ColumnCount
import org.readium.r2.navigator.preferences.Fit
import org.readium.r2.navigator.preferences.FontFamily
import org.readium.r2.navigator.preferences.Spread
import org.readium.r2.navigator.preferences.TextAlign
import org.readium.r2.navigator.preferences.Theme

@RunWith(AndroidJUnit4::class)
class PreferencesHelpersTest {

    private val resources: Resources = mockk()

    @Test
    fun `formatFontFamily returns correct string`() {
        every { resources.getString(R.string.font_family_sans_serif) } returns "Sans Serif"
        every { resources.getString(R.string.font_family_serif) } returns "Serif"
        every { resources.getString(R.string.font_family_open_dyslexic) } returns "Open Dyslexic"

        assertEquals(
            "Sans Serif",
            formatFontFamily(fontFamily = FontFamily.SANS_SERIF, resources = resources),
        )
        assertEquals(
            "Serif",
            formatFontFamily(fontFamily = FontFamily.SERIF, resources = resources),
        )
        assertEquals(
            "Open Dyslexic",
            formatFontFamily(fontFamily = FontFamily.OPEN_DYSLEXIC, resources = resources),
        )

        // Test unknown/null
        assertEquals("Sans Serif", formatFontFamily(fontFamily = null, resources = resources))
    }

    @Test
    fun `formatTextAlign returns correct string`() {
        every { resources.getString(R.string.text_align_start) } returns "Start"
        every { resources.getString(R.string.text_align_justify) } returns "Justify"

        assertEquals("Start", formatTextAlign(textAlign = TextAlign.START, resources = resources))
        assertEquals(
            "Justify",
            formatTextAlign(textAlign = TextAlign.JUSTIFY, resources = resources),
        )

        // Test unknown/null
        assertEquals("Start", formatTextAlign(textAlign = null, resources = resources))
    }

    @Test
    fun `formatTheme returns correct string`() {
        every { resources.getString(R.string.theme_light) } returns "Light"
        every { resources.getString(R.string.theme_dark) } returns "Dark"
        every { resources.getString(R.string.theme_sepia) } returns "Sepia"

        assertEquals("Light", formatTheme(theme = Theme.LIGHT, resources = resources))
        assertEquals("Dark", formatTheme(theme = Theme.DARK, resources = resources))
        assertEquals("Sepia", formatTheme(theme = Theme.SEPIA, resources = resources))
    }

    @Test
    fun `formatFit returns correct string`() {
        every { resources.getString(R.string.fit_contain) } returns "Contain"
        every { resources.getString(R.string.fit_cover) } returns "Cover"

        assertEquals("Contain", formatFit(fit = Fit.CONTAIN, resources = resources))
        assertEquals("Cover", formatFit(fit = Fit.COVER, resources = resources))
    }

    @Test
    fun `formatScrollAxis returns correct string`() {
        every { resources.getString(R.string.scroll_axis_vertical) } returns "Vertical"
        every { resources.getString(R.string.scroll_axis_horizontal) } returns "Horizontal"

        assertEquals("Vertical", formatScrollAxis(axis = Axis.VERTICAL, resources = resources))
        assertEquals("Horizontal", formatScrollAxis(axis = Axis.HORIZONTAL, resources = resources))
    }

    @Test
    fun `formatSpread returns correct string`() {
        every { resources.getString(R.string.spread_auto) } returns "Auto"
        every { resources.getString(R.string.spread_never) } returns "Never"

        assertEquals("Auto", formatSpread(spread = Spread.AUTO, resources = resources))
        assertEquals("Never", formatSpread(spread = Spread.NEVER, resources = resources))
    }

    @Test
    fun `formatColumnCount returns correct string`() {
        every { resources.getString(R.string.column_count_auto) } returns "Auto"
        every { resources.getString(R.string.column_count_one) } returns "One"
        every { resources.getString(R.string.column_count_two) } returns "Two"

        assertEquals("Auto", formatColumnCount(count = ColumnCount.AUTO, resources = resources))
        assertEquals("One", formatColumnCount(count = ColumnCount.ONE, resources = resources))
        assertEquals("Two", formatColumnCount(count = ColumnCount.TWO, resources = resources))

        // Test null
        assertEquals("Auto", formatColumnCount(count = null, resources = resources))
    }
}
