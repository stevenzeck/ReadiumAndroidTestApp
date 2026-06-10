package com.example.readiumandroidtestapp.core.data.repository

import android.net.Uri
import com.example.readiumandroidtestapp.core.domain.model.Book
import com.example.readiumandroidtestapp.core.domain.model.Bookmark
import com.example.readiumandroidtestapp.core.domain.model.ReaderAnnotation
import com.example.readiumandroidtestapp.core.domain.repository.BookRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import org.readium.r2.shared.publication.Locator
import org.readium.r2.shared.publication.Publication
import org.readium.r2.shared.util.AbsoluteUrl
import org.readium.r2.shared.util.Try

class FakeBookRepository : BookRepository {

    override val books: Flow<List<Book>> field = MutableStateFlow<List<Book>>(emptyList())

    fun addBooks(vararg newBooks: Book) {
        books.value += newBooks
    }

    override suspend fun deleteBook(bookId: Long): Try<Unit, Exception> {
        val current = books.value
        val new = current.filter { it.id != bookId }

        return if (current.size != new.size) {
            books.value = new
            Try.success(success = Unit)
        } else {
            Try.failure(failure = Exception("Not found"))
        }
    }

    override suspend fun addBook(url: AbsoluteUrl) = TODO()
    override suspend fun addBook(uri: Uri) = TODO()
    override suspend fun saveProgression(bookId: Long, locator: String) = TODO()
    override suspend fun get(bookId: Long) = books.value.find { it.id == bookId }
    override fun bookmarksForBook(bookId: Long) = flowOf(emptyList<Bookmark>())
    override suspend fun insertBookmark(bookId: Long, publication: Publication, locator: Locator) =
        0L

    override suspend fun deleteBookmark(bookmarkId: Long) {}
    override fun annotationsForBook(bookId: Long) = flowOf(emptyList<ReaderAnnotation>())
    override suspend fun addAnnotation(
        bookId: Long,
        style: ReaderAnnotation.Style,
        tint: Int,
        locator: Locator,
        annotation: String,
    ) = 0L

    override suspend fun updateAnnotationNote(id: Long, note: String) {}
    override suspend fun updateAnnotationStyle(
        id: Long,
        style: ReaderAnnotation.Style,
        tint: Int,
    ) {
    }

    override suspend fun deleteAnnotation(id: Long) {}
}
