package com.example.readiumandroidtestapp.features.reader.domain

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.readiumandroidtestapp.core.domain.repository.BookRepository
import com.example.readiumandroidtestapp.core.domain.model.Highlight
import io.mockk.coVerify
import io.mockk.every
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
import org.readium.r2.shared.util.Url
import org.readium.r2.shared.util.mediatype.MediaType

@RunWith(AndroidJUnit4::class)
class DefaultReaderDecorationManagerTest {

    private val bookRepository: BookRepository = mockk(relaxed = true)
    private val manager = DefaultReaderDecorationManager(bookRepository)

    @Test
    fun `decorationFlow maps highlights to decorations`() = runTest {
        val locator = Locator(href = Url(url = "chap1")!!, mediaType = MediaType("text/html")!!)
        val highlight = Highlight(
            bookId = 10L,
            style = Highlight.Style.HIGHLIGHT,
            tint = 123,
            locator = locator,
            annotation = "Note",
        ).apply { id = 1L }

        every { bookRepository.highlightsForBook(bookId = 10L) } returns flowOf(listOf(highlight))

        val decorations = manager.decorationFlow(bookId = 10L).first()

        assertEquals(1, decorations.size)
        assertEquals("1", decorations[0].id)
        assertEquals(locator, decorations[0].locator)
        assertTrue(decorations[0].style is Decoration.Style.Highlight)
    }

    @Test
    fun `onHighlightAction shows dialog`() {
        val locator =
            Locator(href = Url(url = "chap1")!!, mediaType = MediaType(string = "text/html")!!)
        manager.onHighlightAction(selection = locator)
        assertTrue(manager.showHighlightDialog.value)
    }

    @Test
    fun `saveHighlight saves to repo and dismisses dialog`() = runTest {
        val locator =
            Locator(href = Url(url = "chap1")!!, mediaType = MediaType(string = "text/html")!!)
        manager.onHighlightAction(selection = locator)

        manager.saveHighlight(bookId = 10L, note = "Note", color = 123)

        coVerify {
            bookRepository.addHighlight(
                bookId = 10L,
                style = Highlight.Style.HIGHLIGHT,
                tint = 123,
                locator = locator,
                annotation = "Note",
            )
        }
        assertFalse(manager.showHighlightDialog.value)
    }
}
