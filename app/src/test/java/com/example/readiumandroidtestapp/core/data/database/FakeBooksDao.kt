package com.example.readiumandroidtestapp.core.data.database

import com.example.readiumandroidtestapp.core.domain.model.Book
import com.example.readiumandroidtestapp.core.domain.model.Bookmark
import com.example.readiumandroidtestapp.core.domain.model.Highlight
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class FakeBooksDao : BooksDao {

    private val books = MutableStateFlow<Map<Long, Book>>(emptyMap())
    private val bookmarks = MutableStateFlow<Map<Long, Bookmark>>(emptyMap())
    private val highlights = MutableStateFlow<Map<Long, Highlight>>(emptyMap())

    // --- Books ---

    override suspend fun insertBook(book: Book): Long {
        val id = if (book.id == 0L) (books.value.keys.maxOrNull() ?: 0L) + 1 else book.id
        val newBook = book.copy(id = id)
        books.update { it + (id to newBook) }
        return id
    }

    override suspend fun deleteBook(bookId: Long) {
        books.update { it - bookId }
    }

    override suspend fun get(bookId: Long): Book? {
        return books.value[bookId]
    }

    override fun getAllBooks(): Flow<List<Book>> {
        return books.map { map ->
            map.values.sortedByDescending { it.creation }
        }
    }

    override suspend fun saveProgression(bookId: Long, locator: String) {
        books.update { currentBooks ->
            val book = currentBooks[bookId] ?: return@update currentBooks
            currentBooks + (bookId to book.copy(progression = locator))
        }
    }

    // --- Bookmarks ---

    override fun getBookmarksForBook(bookId: Long): Flow<List<Bookmark>> {
        return bookmarks.map { map ->
            map.values.filter { it.bookId == bookId }
        }
    }

    override suspend fun insertBookmark(bookmark: Bookmark): Long {
        val id = (bookmarks.value.keys.maxOrNull() ?: 0L) + 1
        val newBookmark = bookmark.copy(id = id)
        bookmarks.update { it + (id to newBookmark) }
        return id
    }

    override suspend fun deleteBookmark(id: Long) {
        bookmarks.update { it - id }
    }

    // --- Highlights ---

    override fun getHighlightsForBook(bookId: Long): Flow<List<Highlight>> {
        return highlights.map { map ->
            map.values.filter { it.bookId == bookId }.sortedBy { it.totalProgression }
        }
    }

    override suspend fun getHighlightById(highlightId: Long): Highlight? {
        return highlights.value[highlightId]
    }

    override suspend fun insertHighlight(highlight: Highlight): Long {
        val id = (highlights.value.keys.maxOrNull() ?: 0L) + 1
        val newHighlight = highlight.copy(id = id)
        highlights.update { it + (id to newHighlight) }
        return id
    }

    override suspend fun updateHighlightAnnotation(id: Long, annotation: String) {
        highlights.update { map ->
            val highlight = map[id] ?: return@update map
            map + (id to highlight.copy(annotation = annotation))
        }
    }

    override suspend fun updateHighlightStyle(id: Long, style: Highlight.Style, tint: Int) {
        highlights.update { map ->
            val highlight = map[id] ?: return@update map
            map + (id to highlight.copy(style = style, tint = tint))
        }
    }

    override suspend fun deleteHighlight(id: Long) {
        highlights.update { it - id }
    }
}
