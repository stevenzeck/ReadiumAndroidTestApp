package com.example.readiumandroidtestapp.features.reader.domain

import com.example.readiumandroidtestapp.core.data.book.BookRepository
import com.example.readiumandroidtestapp.core.domain.model.Highlight
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.readium.r2.navigator.Decoration
import org.readium.r2.shared.publication.Locator
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class ReaderDecorationManagerTest {

    private val bookRepository: BookRepository = mockk()
    private val manager = DefaultReaderDecorationManager(bookRepository = bookRepository)

    @Test
    fun `decorationFlow returns empty list when bookId is null`() = runTest {
        val result = manager.decorationFlow(bookId = null).first()
        assertTrue(result.isEmpty())
    }

    @Test
    fun `decorationFlow returns mapped decorations from repository`() = runTest {
        val bookId = 1L
        // Using primary constructor to avoid Locator complexity in test setup
        // and ensure id is set.
        val highlight = Highlight(
            bookId = bookId,
            style = Highlight.Style.HIGHLIGHT,
            tint = 123456,
            href = "http://example.com/chapter1",
            type = "text/html",
            title = "Chapter 1",
            annotation = "Test annotation",
        ).apply { id = 100L }

        coEvery { bookRepository.highlightsForBook(bookId = bookId) } returns flowOf(
            value = listOf(
                highlight,
            ),
        )

        val result = manager.decorationFlow(bookId = bookId).first()

        assertEquals(1, result.size)
        val decoration = result.first()
        assertEquals("100", decoration.id)
        // Check that the decoration locator matches the highlight's locator
        // Compare the objects produced by Highlight.locator
        assertEquals(highlight.locator, decoration.locator)
        assertTrue(decoration.style is Decoration.Style.Highlight)
        assertEquals(123456, (decoration.style as Decoration.Style.Highlight).tint)
    }

    @Test
    fun `onHighlightAction shows dialog`() = runTest {
        val locator = mockk<Locator>(relaxed = true)

        manager.onHighlightAction(selection = locator)

        assertTrue(manager.showHighlightDialog.value)
    }

    @Test
    fun `dismissHighlightDialog hides dialog`() = runTest {
        val locator = mockk<Locator>(relaxed = true)
        manager.onHighlightAction(selection = locator)
        assertTrue(manager.showHighlightDialog.value)

        manager.dismissHighlightDialog()

        assertFalse(manager.showHighlightDialog.value)
    }

    @Test
    fun `saveHighlight saves to repository and dismisses dialog`() = runTest {
        val bookId = 1L
        val note = "My Note"
        val color = 123456
        val locator = mockk<Locator>(relaxed = true)

        // Setup state
        manager.onHighlightAction(selection = locator)

        coEvery {
            bookRepository.addHighlight(
                bookId = bookId,
                style = Highlight.Style.HIGHLIGHT,
                tint = color,
                locator = locator,
                annotation = note,
            )
        } returns 101L

        manager.saveHighlight(bookId = bookId, note = note, color = color)

        coVerify {
            bookRepository.addHighlight(
                bookId = bookId,
                style = Highlight.Style.HIGHLIGHT,
                tint = color,
                locator = locator,
                annotation = note,
            )
        }

        assertFalse(manager.showHighlightDialog.value)
    }
}
