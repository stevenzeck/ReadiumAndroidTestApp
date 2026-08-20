package com.example.readiumandroidtestapp.core.data.repository

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.readiumandroidtestapp.core.data.book.BookImporter
import com.example.readiumandroidtestapp.core.data.database.FakeBooksDao
import com.example.readiumandroidtestapp.core.data.storage.StorageManager
import com.example.readiumandroidtestapp.core.domain.model.Book
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.readium.r2.shared.util.mediatype.MediaType
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class BookRepositoryTest {

    private lateinit var context: Context
    private lateinit var storageManager: StorageManager
    private val booksDao = FakeBooksDao()
    private val bookImporter: BookImporter = mockk()
    private lateinit var repository: BookRepository

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        storageManager = StorageManager(context = context)
        repository = BookRepository(
            booksDao = booksDao,
            bookImporter = bookImporter,
            storageManager = storageManager,
            ioDispatcher = UnconfinedTestDispatcher(),
        )
    }

    @Test
    fun `deleteBook removes book from database and storage`() = runTest {
        // Arrange
        val bookFile = File(storageManager.filesDir, "test_book.epub")
        bookFile.createNewFile()
        assertTrue("File should exist before test", bookFile.exists())

        // Add the book to the Fake Database
        val book = Book(
            id = 1L,
            href = bookFile.absolutePath,
            title = "Test Book",
            identifier = "id",
            mediaType = MediaType.EPUB,
            cover = null,
        )
        booksDao.insertBook(book = book)
        assertNotNull(booksDao.get(bookId = 1L))

        // Act
        val result = repository.deleteBook(bookId = 1L)

        // Assert
        assertTrue("Result should be success", result.isSuccess)
        assertNull("Book should be gone from DB", booksDao.get(bookId = 1L))
        assertFalse("File should be deleted from disk", bookFile.exists())
    }

    @Test
    fun `saveProgression updates the book entity in database`() = runTest {
        // Arrange
        val book = Book(
            id = 1L,
            href = "href",
            title = "Title",
            identifier = "id",
            mediaType = MediaType.EPUB,
            cover = null,
        )
        booksDao.insertBook(book = book)

        // Act
        val locatorJson = "{ \"progression\": 0.5 }"
        repository.saveProgression(bookId = 1L, locator = locatorJson)

        // Assert
        val updatedBook = booksDao.get(bookId = 1L)
        assertEquals(locatorJson, updatedBook?.progression)
    }
}
