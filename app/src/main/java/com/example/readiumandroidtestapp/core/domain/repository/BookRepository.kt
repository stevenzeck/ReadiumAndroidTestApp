package com.example.readiumandroidtestapp.core.domain.repository

import android.net.Uri
import androidx.annotation.ColorInt
import com.example.readiumandroidtestapp.core.data.book.ImportError
import com.example.readiumandroidtestapp.core.domain.model.Book
import com.example.readiumandroidtestapp.core.domain.model.Bookmark
import com.example.readiumandroidtestapp.core.domain.model.ReaderAnnotation
import kotlinx.coroutines.flow.Flow
import org.readium.r2.shared.publication.Locator
import org.readium.r2.shared.publication.Publication
import org.readium.r2.shared.util.AbsoluteUrl
import org.readium.r2.shared.util.Try

interface BookRepository {
    val books: Flow<List<Book>>
    suspend fun addBook(url: AbsoluteUrl): Try<Book, ImportError>
    suspend fun addBook(uri: Uri): Try<Book, ImportError>
    suspend fun deleteBook(bookId: Long): Try<Unit, Exception>
    suspend fun saveProgression(bookId: Long, locator: String): Try<Unit, Exception>
    suspend fun get(bookId: Long): Book?
    fun bookmarksForBook(bookId: Long): Flow<List<Bookmark>>
    suspend fun insertBookmark(bookId: Long, publication: Publication, locator: Locator): Long
    suspend fun deleteBookmark(bookmarkId: Long)
    fun annotationsForBook(bookId: Long): Flow<List<ReaderAnnotation>>
    suspend fun addAnnotation(
        bookId: Long,
        style: ReaderAnnotation.Style,
        @ColorInt tint: Int,
        locator: Locator,
        annotation: String,
    ): Long

    suspend fun updateAnnotationNote(id: Long, note: String)
    suspend fun updateAnnotationStyle(id: Long, style: ReaderAnnotation.Style, @ColorInt tint: Int)
    suspend fun deleteAnnotation(id: Long)
}
