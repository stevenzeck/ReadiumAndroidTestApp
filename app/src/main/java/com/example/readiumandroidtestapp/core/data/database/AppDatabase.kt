package com.example.readiumandroidtestapp.core.data.database

import androidx.room3.ColumnTypeConverters
import androidx.room3.Database
import androidx.room3.RoomDatabase
import com.example.readiumandroidtestapp.core.domain.model.Book
import com.example.readiumandroidtestapp.core.domain.model.Bookmark
import com.example.readiumandroidtestapp.core.domain.model.Catalog
import com.example.readiumandroidtestapp.core.domain.model.ReaderAnnotation
import com.example.readiumandroidtestapp.core.domain.model.ReaderAnnotationConverters

@Database(
    entities = [Book::class, Bookmark::class, ReaderAnnotation::class, Catalog::class],
    version = 1,
    exportSchema = false,
)
@ColumnTypeConverters(
    ReaderAnnotationConverters::class,
)
/**
 * The main Room database for the application.
 *
 * It manages the persistence of:
 * - [Book]: The metadata of imported publications.
 * - [Bookmark]: User bookmarks associated with a book.
 * - [ReaderAnnotation]: User annotations associated with a book.
 * - [Catalog]: External OPDS catalog feeds.
 */
abstract class AppDatabase : RoomDatabase() {

    abstract fun booksDao(): BooksDao

    abstract fun catalogDao(): CatalogDao
}
