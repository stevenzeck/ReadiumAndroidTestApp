package com.example.readiumandroidtestapp.core.data.book

import android.net.Uri
import androidx.annotation.ColorInt
import com.example.readiumandroidtestapp.core.data.database.BooksDao
import com.example.readiumandroidtestapp.core.data.di.IoDispatcher
import com.example.readiumandroidtestapp.core.domain.model.Book
import com.example.readiumandroidtestapp.core.domain.model.Bookmark
import com.example.readiumandroidtestapp.core.domain.model.Highlight
import com.example.readiumandroidtestapp.core.domain.storage.StorageGateway
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
 *
 * ERROR HANDLING PATTERN:
 * This repository (and the app in general) uses Railway Oriented Programming with the [Try] monad.
 * Instead of throwing exceptions, methods return a `Try<Success, Failure>`.
 *
 * - **Chaining:** Use `.flatMap { ... }` to chain operations that can fail. If any step returns a `Try.Failure`,
 *   the chain stops, and the failure is propagated down the line.
 *
 * - **Error Mapping:** Use `.mapFailure { ... }` to convert lower-level errors (like HTTP or IO errors)
 *   into domain-specific errors (like `ImportError.Network`).
 *
 * - **Resource Management:** Crucially, when working with Readium's `Publication` objects (which may hold open
 *   file handles or memory resources), use a `try/finally` block inside your chain or ensure `publication.close()`
 *   is called. The `Try` monad does not automatically close resources.
 */
@Singleton
class DefaultBookRepository @Inject constructor(
    private val booksDao: BooksDao,
    private val bookImporter: BookImporter,
    private val storageGateway: StorageGateway,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : BookRepository {

    override val books: Flow<List<Book>> = booksDao.getAllBooks()

    /**
     * Imports a book from a remote URL.
     *
     * @param url The URL of the book to import.
     *
     * @return [Try.Success] with the imported [Book], or [Try.Failure] with an [ImportError] (Network, Storage, etc.).
     */
    override suspend fun addBook(url: AbsoluteUrl): Try<Book, ImportError> {
        return bookImporter.importFromUrl(url = url)
    }

    /**
     * Imports a book from a local Content URI (e.g., from the file picker).
     *
     * @param uri The URI of the book to import.
     *
     * @return [Try.Success] with the imported [Book], or [Try.Failure] with an [ImportError].
     */
    override suspend fun addBook(uri: Uri): Try<Book, ImportError> {
        return bookImporter.importFromUri(uri = uri)
    }

    /**
     * Deletes a book from the database and removes its associated files (eBook file and cover image) from storage.
     *
     * @param bookId The ID of the book to delete.
     *
     * @return [Try.Success] if the deletion was successful (or if the files were already missing but DB delete worked),
     * or [Try.Failure] if a database error occurred.
     */
    override suspend fun deleteBook(bookId: Long): Try<Unit, Exception> =
        withContext(context = ioDispatcher) {
            try {
                val book = booksDao.get(bookId = bookId)
                    ?: return@withContext Try.failure(failure = Exception("Book not found"))

                // First, remove the entry from the database.
                booksDao.deleteBook(bookId = bookId)

                // Then, attempt to clean up the physical files.
                // We wrap this in a try-catch because even if file deletion fails,
                // the "business transaction" (removing the book from the library) is effectively complete.
                try {
                    storageGateway.deleteFile(path = book.href)
                    book.cover?.let { storageGateway.deleteFile(path = it) }
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
     *
     * @param bookId The ID of the book to update.
     * @param locator The JSON string representation of the [org.readium.r2.shared.publication.Locator].
     *
     * @return [Try.Success] if the update was successful, or [Try.Failure] if a database error occurred.
     */
    override suspend fun saveProgression(bookId: Long, locator: String): Try<Unit, Exception> =
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
     *
     * @param bookId The ID of the book to retrieve.
     *
     * @return The [Book] with the given ID, or null if not found.
     */
    override suspend fun get(bookId: Long): Book? =
        withContext(context = ioDispatcher) {
            booksDao.get(bookId)
        }

    /**
     * Gets all bookmarks for a book by its ID.
     *
     * @param bookId The ID of the book.
     *
     * @return A flow emitting a list of [Bookmark]s for the given book.
     */
    override fun bookmarksForBook(bookId: Long): Flow<List<Bookmark>> =
        booksDao.getBookmarksForBook(bookId)

    /**
     * Inserts a bookmark for a book.
     *
     * @param bookId The ID of the book.
     * @param publication The publication containing the bookmark.
     * @param locator The [org.readium.r2.shared.publication.Locator] of the bookmark.
     *
     * @return The ID of the inserted bookmark.
     */
    override suspend fun insertBookmark(
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

        return booksDao.insertBookmark(bookmark)
    }

    /**
     * Deletes a bookmark by its ID.
     *
     * @param bookmarkId The ID of the bookmark to delete.
     */
    override suspend fun deleteBookmark(bookmarkId: Long) = booksDao.deleteBookmark(bookmarkId)

    /**
     * Gets all highlights for a book by its ID.
     *
     * @param bookId The ID of the book.
     *
     * @return A flow emitting a list of [Highlight]s for the given book.
     */
    override fun highlightsForBook(bookId: Long): Flow<List<Highlight>> =
        booksDao.getHighlightsForBook(bookId)

    /**
     * Adds a highlight for a book.
     *
     * @param bookId The ID of the book.
     * @param style The style of the highlight.
     * @param tint The color of the highlight.
     * @param locator The [org.readium.r2.shared.publication.Locator] of the highlighted area.
     * @param annotation The annotation or note associated with the highlight.
     *
     * @return The ID of the inserted highlight.
     */
    override suspend fun addHighlight(
        bookId: Long,
        style: Highlight.Style,
        @ColorInt tint: Int,
        locator: Locator,
        annotation: String,
    ): Long = booksDao.insertHighlight(Highlight(bookId, style, tint, locator, annotation))

    /**
     * Updates the annotation of a highlight.
     *
     * @param id The ID of the highlight.
     * @param annotation The new annotation or note.
     */
    override suspend fun updateHighlightAnnotation(id: Long, annotation: String) {
        booksDao.updateHighlightAnnotation(id, annotation)
    }

    /**
     * Updates the style of a highlight.
     *
     * @param id The ID of the highlight.
     * @param style The new style of the highlight.
     */
    override suspend fun updateHighlightStyle(
        id: Long,
        style: Highlight.Style,
        @ColorInt tint: Int,
    ) {
        booksDao.updateHighlightStyle(id, style, tint)
    }

    /**
     * Deletes a highlight by its ID.
     *
     * @param id The ID of the highlight to delete.
     */
    override suspend fun deleteHighlight(id: Long) = booksDao.deleteHighlight(id)
}
