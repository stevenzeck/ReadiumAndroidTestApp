package com.example.readiumandroidtestapp.core.data.repository

import android.net.Uri
import androidx.annotation.ColorInt
import com.example.readiumandroidtestapp.core.data.book.BookImporter
import com.example.readiumandroidtestapp.core.data.book.ImportError
import com.example.readiumandroidtestapp.core.data.database.BooksDao
import com.example.readiumandroidtestapp.core.data.di.IoDispatcher
import com.example.readiumandroidtestapp.core.data.storage.StorageManager
import com.example.readiumandroidtestapp.core.domain.model.Book
import com.example.readiumandroidtestapp.core.domain.model.Bookmark
import com.example.readiumandroidtestapp.core.domain.model.ReaderAnnotation
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.readium.r2.shared.publication.Locator
import org.readium.r2.shared.publication.Publication
import org.readium.r2.shared.publication.indexOfFirstWithHref
import org.readium.r2.shared.util.AbsoluteUrl
import org.readium.r2.shared.util.Try
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing [Book] entities.
 */
@Singleton
class BookRepository @Inject constructor(
    private val booksDao: BooksDao,
    private val bookImporter: BookImporter,
    private val storageManager: StorageManager,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {

    val books: Flow<List<Book>> = booksDao.getAllBooks()

    /**
     * Imports a book from a remote URL.
     */
    suspend fun addBook(url: AbsoluteUrl): Try<Book, ImportError> {
        return bookImporter.importFromUrl(url = url)
    }

    /**
     * Imports a book from a local Content URI.
     */
    suspend fun addBook(uri: Uri): Try<Book, ImportError> {
        return bookImporter.importFromUri(uri = uri)
    }

    /**
     * Deletes a book from the database and removes its associated files.
     */
    suspend fun deleteBook(bookId: Long): Try<Unit, Exception> =
        withContext(context = ioDispatcher) {
            try {
                val book = booksDao.get(bookId = bookId)
                    ?: return@withContext Try.failure(failure = Exception("Book not found"))

                booksDao.deleteBook(bookId = bookId)

                try {
                    storageManager.deleteFile(path = book.href)
                    book.cover?.let { storageManager.deleteFile(path = it) }
                } catch (e: Exception) {
                    Timber.w(t = e, message = "Failed to clean up files for book $bookId")
                }

                Try.success(success = Unit)
            } catch (e: Exception) {
                Try.failure(failure = e)
            }
        }

    /**
     * Updates the saved reading progression for a book.
     */
    suspend fun saveProgression(bookId: Long, locator: String): Try<Unit, Exception> =
        withContext(context = ioDispatcher) {
            try {
                booksDao.saveProgression(bookId = bookId, locator = locator)
                Try.success(success = Unit)
            } catch (e: Exception) {
                Try.failure(failure = e)
            }
        }

    /**
     * Retrieves a book by its ID.
     */
    suspend fun get(bookId: Long): Book? =
        withContext(context = ioDispatcher) {
            booksDao.get(bookId = bookId)
        }

    /**
     * Gets all bookmarks for a book by its ID.
     */
    fun bookmarksForBook(bookId: Long): Flow<List<Bookmark>> =
        booksDao.getBookmarksForBook(bookId = bookId)

    /**
     * Inserts a bookmark for a book.
     */
    suspend fun insertBookmark(
        bookId: Long,
        publication: Publication,
        locator: Locator,
    ): Long {
        val resource = publication.readingOrder.indexOfFirstWithHref(locator.href)!!
        val bookmark = Bookmark(
            creation = System.currentTimeMillis(),
            bookId = bookId,
            resourceIndex = resource.toLong(),
            resourceHref = locator.href.toString(),
            resourceType = locator.mediaType.toString(),
            resourceTitle = locator.title.orEmpty(),
            location = locator.locations.toJSON().toString(),
            locatorText = Locator.Text().toJSON().toString(),
        )

        return booksDao.insertBookmark(bookmark = bookmark)
    }

    /**
     * Deletes a bookmark by its ID.
     */
    suspend fun deleteBookmark(bookmarkId: Long) = booksDao.deleteBookmark(id = bookmarkId)

    /**
     * Gets all annotations for a book by its ID.
     */
    fun annotationsForBook(bookId: Long): Flow<List<ReaderAnnotation>> =
        booksDao.getAnnotationsForBook(bookId = bookId)

    /**
     * Adds an annotation for a book.
     */
    suspend fun addAnnotation(
        bookId: Long,
        style: ReaderAnnotation.Style,
        @ColorInt tint: Int,
        locator: Locator,
        annotation: String,
    ): Long = booksDao.insertAnnotation(
        annotation = ReaderAnnotation(
            bookId = bookId,
            style = style,
            tint = tint,
            locator = locator,
            annotation = annotation,
        ),
    )

    /**
     * Updates the annotation note.
     */
    suspend fun updateAnnotationNote(id: Long, note: String) {
        booksDao.updateAnnotationNote(id = id, note = note)
    }

    /**
     * Updates the style of an annotation.
     */
    suspend fun updateAnnotationStyle(
        id: Long,
        style: ReaderAnnotation.Style,
        @ColorInt tint: Int,
    ) {
        booksDao.updateAnnotationStyle(id = id, style = style, tint = tint)
    }

    /**
     * Deletes an annotation by its ID.
     */
    suspend fun deleteAnnotation(id: Long) = booksDao.deleteAnnotation(id = id)
}
