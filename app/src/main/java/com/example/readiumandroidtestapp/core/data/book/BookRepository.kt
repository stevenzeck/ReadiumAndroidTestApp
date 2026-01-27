package com.example.readiumandroidtestapp.core.data.book

import android.net.Uri
import androidx.annotation.ColorInt
import com.example.readiumandroidtestapp.core.domain.model.Book
import com.example.readiumandroidtestapp.core.domain.model.Bookmark
import com.example.readiumandroidtestapp.core.domain.model.Highlight
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
    fun highlightsForBook(bookId: Long): Flow<List<Highlight>>
    suspend fun addHighlight(
        bookId: Long,
        style: Highlight.Style,
        @ColorInt tint: Int,
        locator: Locator,
        annotation: String,
    ): Long

    suspend fun updateHighlightAnnotation(id: Long, annotation: String)
    suspend fun updateHighlightStyle(id: Long, style: Highlight.Style, @ColorInt tint: Int)
    suspend fun deleteHighlight(id: Long)
}
