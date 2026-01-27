package com.example.readiumandroidtestapp.core.data.book

import android.net.Uri
import com.example.readiumandroidtestapp.core.data.database.BooksDao
import com.example.readiumandroidtestapp.core.domain.model.Book
import com.example.readiumandroidtestapp.core.domain.model.Bookmark
import com.example.readiumandroidtestapp.core.domain.model.Highlight
import com.example.readiumandroidtestapp.core.domain.storage.StorageGateway
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.readium.r2.shared.publication.Locator
import org.readium.r2.shared.util.AbsoluteUrl
import org.readium.r2.shared.util.Try

class BookRepositoryTest {

    private val booksDao: BooksDao = mockk(relaxed = true)
    private val bookImporter: BookImporter = mockk()
    private val storageGateway: StorageGateway = mockk(relaxed = true)

    private val repository = DefaultBookRepository(
        booksDao = booksDao,
        bookImporter = bookImporter,
        storageGateway = storageGateway,
    )

    @Test
    fun `addBook from URL returns success`() = runTest {
        val url = mockk<AbsoluteUrl>()
        val expectedBook = mockk<Book>()
        coEvery { bookImporter.importFromUrl(url = url) } returns Try.success(success = expectedBook)

        val result = repository.addBook(url = url)

        assertTrue(result.isSuccess)
        assertEquals(expectedBook, result.getOrNull())
    }

    @Test
    fun `addBook from URI returns success`() = runTest {
        val uri = mockk<Uri>()
        val expectedBook = mockk<Book>()
        coEvery { bookImporter.importFromUri(uri = uri) } returns Try.success(success = expectedBook)

        val result = repository.addBook(uri = uri)

        assertTrue(result.isSuccess)
        assertEquals(expectedBook, result.getOrNull())
    }

    @Test
    fun `deleteBook success deletes from dao and storage`() = runTest {
        val bookId = 1L
        val bookPath = "/path/to/book.epub"
        val coverPath = "/path/to/cover.jpg"
        val book = Book(
            id = bookId,
            creation = 0L,
            href = bookPath,
            title = "Test Book",
            identifier = "id",
            cover = coverPath,
            rawMediaType = "application/epub+zip",
        )

        coEvery { booksDao.get(bookId = bookId) } returns book
        coEvery { booksDao.deleteBook(bookId = bookId) } returns Unit
        every { storageGateway.deleteFile(path = any()) } returns true

        val result = repository.deleteBook(bookId = bookId)

        assertTrue(result.isSuccess)
        coVerify { booksDao.deleteBook(bookId = bookId) }
        coVerify { storageGateway.deleteFile(path = bookPath) }
        coVerify { storageGateway.deleteFile(path = coverPath) }
    }

    @Test
    fun `deleteBook handles storage failure gracefully`() = runTest {
        val bookId = 1L
        val bookPath = "/path/to/book.epub"
        val book = Book(
            id = bookId,
            creation = 0L,
            href = bookPath,
            title = "Test Book",
            identifier = "id",
            cover = null,
            rawMediaType = "application/epub+zip",
        )

        coEvery { booksDao.get(bookId = bookId) } returns book
        coEvery { booksDao.deleteBook(bookId = bookId) } returns Unit
        every { storageGateway.deleteFile(path = any()) } throws RuntimeException("File error")

        val result = repository.deleteBook(bookId = bookId)

        assertTrue(result.isSuccess)
        coVerify { booksDao.deleteBook(bookId = bookId) }
    }

    @Test
    fun `deleteBook returns failure if book not found`() = runTest {
        val bookId = 1L
        coEvery { booksDao.get(bookId = bookId) } returns null

        val result = repository.deleteBook(bookId = bookId)

        assertTrue(result.isFailure)
        assertEquals("Book not found", result.failureOrNull()?.message)
    }

    @Test
    fun `saveProgression calls dao`() = runTest {
        val bookId = 1L
        val locatorJson = "{}"

        val result = repository.saveProgression(bookId = bookId, locator = locatorJson)

        assertTrue(result.isSuccess)
        coVerify { booksDao.saveProgression(bookId = bookId, locator = locatorJson) }
    }

    @Test
    fun `get returns book from dao`() = runTest {
        val bookId = 1L
        val expectedBook = mockk<Book>()
        coEvery { booksDao.get(bookId = bookId) } returns expectedBook

        val result = repository.get(bookId = bookId)

        assertEquals(expectedBook, result)
    }

    @Test
    fun `bookmarksForBook returns flow from dao`() = runTest {
        val bookId = 1L
        val expectedBookmarks = listOf(mockk<Bookmark>())
        every { booksDao.getBookmarksForBook(bookId = bookId) } returns flowOf(value = expectedBookmarks)

        val result = repository.bookmarksForBook(bookId = bookId).first()

        assertEquals(expectedBookmarks, result)
    }


    @Test
    fun `highlightsForBook returns flow from dao`() = runTest {
        val bookId = 1L
        val expectedHighlights = listOf(mockk<Highlight>())
        every { booksDao.getHighlightsForBook(bookId = bookId) } returns flowOf(value = expectedHighlights)

        val result = repository.highlightsForBook(bookId = bookId).first()

        assertEquals(expectedHighlights, result)
    }

    @Test
    fun `addHighlight calls dao`() = runTest {
        val bookId = 1L
        val locator = mockk<Locator>(relaxed = true)
        coEvery { booksDao.insertHighlight(highlight = any()) } returns 20L

        val id = repository.addHighlight(
            bookId = bookId,
            style = Highlight.Style.HIGHLIGHT,
            tint = 123,
            locator = locator,
            annotation = "annotation",
        )

        assertEquals(20L, id)
        coVerify { booksDao.insertHighlight(highlight = any()) }
    }
}
