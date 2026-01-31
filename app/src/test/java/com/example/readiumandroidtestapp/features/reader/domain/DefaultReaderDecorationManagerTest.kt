package com.example.readiumandroidtestapp.features.reader.domain

import com.example.readiumandroidtestapp.core.data.book.BookRepository
import com.example.readiumandroidtestapp.core.domain.model.Highlight
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.readium.r2.shared.publication.Locator
import org.readium.r2.shared.util.Url
import org.readium.r2.shared.util.mediatype.MediaType
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
@OptIn(ExperimentalCoroutinesApi::class)
class DefaultReaderDecorationManagerTest {

    private val bookRepository: BookRepository = mockk(relaxed = true)
    private lateinit var manager: DefaultReaderDecorationManager
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher = testDispatcher)
        manager = DefaultReaderDecorationManager(bookRepository = bookRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `decorationFlow returns mapped decorations`() = runTest {
        val bookId = 1L
        val locator =
            Locator(href = Url(url = "href")!!, mediaType = MediaType(string = "text/html")!!)
        val highlight = Highlight(
            bookId = bookId,
            style = Highlight.Style.HIGHLIGHT,
            tint = 123,
            locator = locator,
            annotation = "note",
        )
        // Set ID manually since we use the secondary constructor
        highlight.id = 1

        every { bookRepository.highlightsForBook(bookId = bookId) } returns flowOf(
            value = listOf(
                highlight,
            ),
        )

        val decorations = manager.decorationFlow(bookId = bookId).first()

        assertEquals(1, decorations.size)
        assertEquals("1", decorations.first().id)
    }

    @Test
    fun `onHighlightAction updates dialog state`() = runTest {
        val locator =
            Locator(href = Url(url = "href")!!, mediaType = MediaType(string = "text/html")!!)
        manager.onHighlightAction(selection = locator)
        assertTrue(manager.showHighlightDialog.value)
    }

    @Test
    fun `dismissHighlightDialog resets state`() = runTest {
        val locator =
            Locator(href = Url(url = "href")!!, mediaType = MediaType(string = "text/html")!!)
        manager.onHighlightAction(selection = locator)
        manager.dismissHighlightDialog()
        assertFalse(manager.showHighlightDialog.value)
    }

    @Test
    fun `saveHighlight saves to repository and dismisses dialog`() = runTest {
        val bookId = 1L
        val locator =
            Locator(href = Url(url = "href")!!, mediaType = MediaType(string = "text/html")!!)
        manager.onHighlightAction(selection = locator)

        manager.saveHighlight(bookId = bookId, note = "note", color = 123)

        coVerify {
            bookRepository.addHighlight(
                bookId = bookId,
                style = Highlight.Style.HIGHLIGHT,
                tint = 123,
                locator = locator,
                annotation = "note",
            )
        }
        assertFalse(manager.showHighlightDialog.value)
    }
}
