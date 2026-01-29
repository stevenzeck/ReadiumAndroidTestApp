package com.example.readiumandroidtestapp.features.reader.ui.utils

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import org.junit.Assert.assertEquals
import org.junit.Test
import org.readium.r2.shared.publication.Locator

class ReaderTextUtilsTest {

    @Test
    fun `toAnnotatedString creates correct string with highlight`() {
        val locatorText = Locator.Text(
            before = "Hello ",
            highlight = "World",
            after = "!",
        )

        val annotatedString = locatorText.toAnnotatedString()

        assertEquals("Hello World!", annotatedString.text)

        // Verify span styles
        val spanStyles = annotatedString.spanStyles
        assertEquals(1, spanStyles.size)

        val span = spanStyles[0]
        assertEquals(6, span.start)
        assertEquals(11, span.end)

        val style = span.item
        assertEquals(Color.Yellow, style.background)
        assertEquals(Color.Black, style.color)
        assertEquals(FontWeight.Bold, style.fontWeight)
    }

    @Test
    fun `toAnnotatedString handles missing parts`() {
        val locatorText = Locator.Text(
            highlight = "Only Highlight",
        )

        val annotatedString = locatorText.toAnnotatedString()

        assertEquals("Only Highlight", annotatedString.text)
        assertEquals(1, annotatedString.spanStyles.size)
        assertEquals(0, annotatedString.spanStyles[0].start)
        assertEquals(14, annotatedString.spanStyles[0].end)
    }

    @Test
    fun `toAnnotatedString handles null highlight`() {
        val locatorText = Locator.Text(
            before = "Before",
            after = "After",
        )

        val annotatedString = locatorText.toAnnotatedString()

        assertEquals("BeforeAfter", annotatedString.text)
        assertEquals(0, annotatedString.spanStyles.size)
    }
}
