package com.example.readiumandroidtestapp.core.data.repository

import android.graphics.Color
import android.net.Uri
import com.example.readiumandroidtestapp.core.data.book.BookImporter
import com.example.readiumandroidtestapp.core.data.database.BooksDao
import com.example.readiumandroidtestapp.core.domain.model.Book
import com.example.readiumandroidtestapp.core.domain.model.Bookmark
import com.example.readiumandroidtestapp.core.domain.model.Highlight
import com.example.readiumandroidtestapp.core.domain.storage.StorageGateway
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.readium.r2.shared.publication.Link
import org.readium.r2.shared.publication.Locator
import org.readium.r2.shared.publication.Publication
import org.readium.r2.shared.util.AbsoluteUrl
import org.readium.r2.shared.util.Try
import org.readium.r2.shared.util.Url
import org.readium.r2.shared.util.mediatype.MediaType
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class DefaultBookRepositoryTest {

    private val booksDao: BooksDao = mockk(relaxed = true)
    private val bookImporter: BookImporter = mockk()
    private val storageGateway: StorageGateway = mockk(relaxed = true)
    private val testDispatcher = UnconfinedTestDispatcher()

    private val repository = DefaultBookRepository(
        booksDao = booksDao,
        bookImporter = bookImporter,
        storageGateway = storageGateway,
        ioDispatcher = testDispatcher,
    )

    @Test
    fun `addBook from Url success`() = runTest {
        val url = AbsoluteUrl("http://example.com/book.epub")!!
        val book = Book(
            id = 1,
            href = "path/to/book.epub",
            title = "Title",
            author = "Author",
            identifier = "id",
            mediaType = MediaType(string = "application/epub+zip")!!,
            cover = null,
        )

        coEvery { bookImporter.importFromUrl(url = url) } returns Try.success(success = book)

        val result = repository.addBook(url = url)

        assertTrue(result.isSuccess)
        assertEquals(book, result.getOrNull())
        coVerify { bookImporter.importFromUrl(url = url) }
    }

    @Test
    fun `addBook from Uri success`() = runTest {
        val uri = Uri.parse("content://com.example/book.epub")
        val book = Book(
            id = 1,
            href = "path/to/book.epub",
            title = "Title",
            author = "Author",
            identifier = "id",
            mediaType = MediaType(string = "application/epub+zip")!!,
            cover = null,
        )

        coEvery { bookImporter.importFromUri(uri = uri) } returns Try.success(success = book)

        val result = repository.addBook(uri = uri)

        assertTrue(result.isSuccess)
        assertEquals(book, result.getOrNull())
        coVerify { bookImporter.importFromUri(uri = uri) }
    }

    @Test
    fun `deleteBook success removes files and db entry`() = runTest {
        val bookId = 1L
        val book = Book(
            id = bookId,
            href = "path/to/book.epub",
            title = "Title",
            author = "Author",
            identifier = "id",
            mediaType = MediaType(string = "application/epub+zip")!!,
            cover = "path/to/cover.jpg",
        )

        coEvery { booksDao.get(bookId = bookId) } returns book
        coEvery { booksDao.deleteBook(bookId = bookId) } just Runs

        val result = repository.deleteBook(bookId = bookId)

        assertTrue(result.isSuccess)
        coVerify { booksDao.deleteBook(bookId = bookId) }
        verify { storageGateway.deleteFile(path = book.href) }
        verify { storageGateway.deleteFile(path = book.cover!!) }
    }

    @Test
    fun `deleteBook returns failure when book not found`() = runTest {
        val bookId = 1L
        coEvery { booksDao.get(bookId = bookId) } returns null

        val result = repository.deleteBook(bookId = bookId)

        assertTrue(result.isFailure)
        coVerify(exactly = 0) { booksDao.deleteBook(bookId = any()) }
    }

    @Test
    fun `deleteBook failure on file deletion is suppressed and returns success`() = runTest {
        val bookId = 1L
        val book = Book(
            id = bookId,
            href = "path/to/book.epub",
            title = "Title",
            author = "Author",
            identifier = "id",
            mediaType = MediaType(string = "application/epub+zip")!!,
            cover = "path/to/cover.jpg",
        )

        coEvery { booksDao.get(bookId = bookId) } returns book
        coEvery { booksDao.deleteBook(bookId = bookId) } just Runs
        every { storageGateway.deleteFile(path = any()) } throws RuntimeException("Delete failed")

        val result = repository.deleteBook(bookId = bookId)

        assertTrue(result.isSuccess)
        coVerify { booksDao.deleteBook(bookId = bookId) }
    }

    @Test
    fun `saveProgression failure returns Try Failure`() = runTest {
        val bookId = 1L
        val locator = "locator-json"
        val exception = RuntimeException("DAO error")

        coEvery {
            booksDao.saveProgression(
                bookId = bookId,
                locator = locator,
            )
        } throws exception

        val result = repository.saveProgression(bookId = bookId, locator = locator)

        assertTrue(result.isFailure)
        assertEquals(exception, (result as Try.Failure).value)
    }

    @Test
    fun `insertBookmark inserts bookmark with correct data`() = runTest {
        val bookId = 1L
        val locator = Locator(
            href = Url(url = "chapter1.html")!!,
            mediaType = MediaType(string = "text/html")!!,
            title = "Chapter 1",
            locations = Locator.Locations(progression = 0.5),
            text = Locator.Text(highlight = "highlight"),
        )
        val publication = mockk<Publication>()

        val link = Link(href = Url(url = "chapter1.html")!!)
        every { publication.readingOrder } returns listOf(link)

        coEvery { booksDao.insertBookmark(bookmark = any()) } returns 123L

        val result = repository.insertBookmark(
            bookId = bookId,
            publication = publication,
            locator = locator,
        )

        assertEquals(123L, result)

        val bookmarkSlot = slot<Bookmark>()
        coVerify { booksDao.insertBookmark(bookmark = capture(lst = bookmarkSlot)) }

        val captured = bookmarkSlot.captured
        assertEquals(bookId, captured.bookId)
        assertEquals("chapter1.html", captured.resourceHref)
        assertEquals(0L, captured.resourceIndex)
        assertEquals("text/html", captured.resourceType)
        assertEquals("Chapter 1", captured.resourceTitle)
    }

    @Test
    fun `addHighlight calls dao with correct data`() = runTest {
        val bookId = 1L
        val style = Highlight.Style.HIGHLIGHT
        val tint = 123456
        val locator = Locator(
            href = Url("chapter1.html")!!,
            mediaType = MediaType(string = "text/html")!!,
            title = "Chapter 1",
        )
        val annotation = "My Note"

        coEvery { booksDao.insertHighlight(highlight = any()) } returns 99L

        val result = repository.addHighlight(
            bookId = bookId,
            style = style,
            tint = tint,
            locator = locator,
            annotation = annotation,
        )

        assertEquals(99L, result)

        val highlightSlot = slot<Highlight>()
        coVerify { booksDao.insertHighlight(highlight = capture(lst = highlightSlot)) }

        val captured = highlightSlot.captured
        assertEquals(bookId, captured.bookId)
        assertEquals(style, captured.style)
        assertEquals(tint, captured.tint)
        assertEquals(locator.href.toString(), captured.href)
        assertEquals(annotation, captured.annotation)
    }
}
