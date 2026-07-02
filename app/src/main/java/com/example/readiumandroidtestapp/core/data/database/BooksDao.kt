package com.example.readiumandroidtestapp.core.data.database

import androidx.annotation.ColorInt
import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import com.example.readiumandroidtestapp.core.domain.model.Book
import com.example.readiumandroidtestapp.core.domain.model.Bookmark
import com.example.readiumandroidtestapp.core.domain.model.ReaderAnnotation
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for books and their associated user data (bookmarks, annotations).
 *
 * This DAO handles:
 * - The storage of [Book] entities.
 * - The One-to-Many relationship between [Book] and [Bookmark].
 * - The One-to-Many relationship between [Book] and [ReaderAnnotation].
 */
@Dao
interface BooksDao {

    /**
     * Inserts a book
     * @param book The book to insert
     * @return ID of the book that was added (primary key)
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: Book): Long

    /**
     * Deletes a book
     * @param bookId The ID of the book
     */
    @Query("DELETE FROM " + Book.TABLE_NAME + " WHERE " + Book.ID + " = :bookId")
    suspend fun deleteBook(bookId: Long)

    /**
     * Retrieve a book from its ID.
     */
    @Query("SELECT * FROM " + Book.TABLE_NAME + " WHERE " + Book.ID + " = :bookId")
    suspend fun get(bookId: Long): Book?

    /**
     * Retrieve all books
     * @return List of books as Flow
     */
    @Query("SELECT * FROM " + Book.TABLE_NAME + " ORDER BY " + Book.CREATION_DATE + " desc")
    fun getAllBooks(): Flow<List<Book>>

    /**
     * Retrieve all bookmarks for a specific book
     * @param bookId The ID of the book
     * @return List of bookmarks for the book as Flow
     */
    @Query("SELECT * FROM " + Bookmark.TABLE_NAME + " WHERE " + Bookmark.BOOK_ID + " = :bookId")
    fun getBookmarksForBook(bookId: Long): Flow<List<Bookmark>>

    /**
     * Retrieve all annotations for a specific book
     */
    @Query(
        "SELECT * FROM ${ReaderAnnotation.TABLE_NAME} WHERE ${ReaderAnnotation.BOOK_ID} = :bookId ORDER BY ${ReaderAnnotation.TOTAL_PROGRESSION} ASC"
    )
    fun getAnnotationsForBook(bookId: Long): Flow<List<ReaderAnnotation>>

    /**
     * Retrieves the annotation with the given ID.
     */
    @Query("SELECT * FROM ${ReaderAnnotation.TABLE_NAME} WHERE ${ReaderAnnotation.ID} = :annotationId")
    suspend fun getAnnotationById(annotationId: Long): ReaderAnnotation?

    /**
     * Inserts a bookmark
     * @param bookmark The bookmark to insert
     * @return The ID of the bookmark that was added (primary key)
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBookmark(bookmark: Bookmark): Long

    /**
     * Inserts an annotation
     * @param annotation The annotation to insert
     * @return The ID of the annotation that was added (primary key)
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnnotation(annotation: ReaderAnnotation): Long

    /**
     * Updates an annotation's note text.
     */
    @Query(
        "UPDATE ${ReaderAnnotation.TABLE_NAME} SET ${ReaderAnnotation.ANNOTATION} = :note WHERE ${ReaderAnnotation.ID} = :id"
    )
    suspend fun updateAnnotationNote(id: Long, note: String)

    /**
     * Updates an annotation's tint and style.
     */
    @Query(
        "UPDATE ${ReaderAnnotation.TABLE_NAME} SET ${ReaderAnnotation.TINT} = :tint, ${ReaderAnnotation.STYLE} = :style WHERE ${ReaderAnnotation.ID} = :id"
    )
    suspend fun updateAnnotationStyle(id: Long, style: ReaderAnnotation.Style, @ColorInt tint: Int)

    /**
     * Deletes a bookmark
     */
    @Query("DELETE FROM " + Bookmark.TABLE_NAME + " WHERE " + Bookmark.ID + " = :id")
    suspend fun deleteBookmark(id: Long)

    /**
     * Deletes the annotation with given id.
     */
    @Query("DELETE FROM ${ReaderAnnotation.TABLE_NAME} WHERE ${ReaderAnnotation.ID} = :id")
    suspend fun deleteAnnotation(id: Long)

    /**
     * Saves book progression
     * @param bookId The book to update
     * @param locator Location of the book
     */
    @Query(
        "UPDATE " + Book.TABLE_NAME + " SET " + Book.PROGRESSION + " = :locator WHERE " + Book.ID + "= :bookId"
    )
    suspend fun saveProgression(bookId: Long, locator: String)
}
