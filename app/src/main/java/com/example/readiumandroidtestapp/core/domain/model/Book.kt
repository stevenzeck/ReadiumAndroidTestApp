package com.example.readiumandroidtestapp.core.domain.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import org.readium.r2.shared.util.AbsoluteUrl
import org.readium.r2.shared.util.mediatype.MediaType

/**
 * The central Entity representing a publication in the library.
 *
 * This class maps directly to the `books` table in the database and stores
 * all metadata necessary to display the book in the bookshelf and open it with the correct reader.
 *
 * @param id The unique ID of the book in the database.
 * @param creation Timestamp of when the book was imported.
 * @param href The location of the book. This can be a local file path (e.g., for imported EPUBs) or a remote URL.
 * @param title The title of the book.
 * @param author The author(s) of the book.
 * @param identifier The unique identifier of the publication (e.g., ISBN or UUID).
 * @param progression A JSON string representing the user's last read position (Locator).
 * @param rawMediaType The string representation of the MIME type (e.g., "application/epub+zip"). Used by Room for persistence.
 * @param cover Path to the local cover image file, if available.
 */
@Serializable
@Entity(tableName = Book.TABLE_NAME)
data class Book(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = ID)
    val id: Long = 0,
    @ColumnInfo(name = CREATION_DATE)
    val creation: Long = System.currentTimeMillis(),
    @ColumnInfo(name = HREF)
    val href: String,
    @ColumnInfo(name = TITLE)
    val title: String?,
    @ColumnInfo(name = AUTHOR)
    val author: String? = null,
    @ColumnInfo(name = IDENTIFIER)
    val identifier: String,
    @ColumnInfo(name = PROGRESSION)
    val progression: String? = null,
    @ColumnInfo(name = MEDIA_TYPE)
    val rawMediaType: String,
    @ColumnInfo(name = COVER)
    val cover: String?,
) {

    constructor(
        id: Long = 0,
        creation: Long = System.currentTimeMillis(),
        href: String,
        title: String?,
        author: String? = null,
        identifier: String,
        progression: String? = null,
        mediaType: MediaType,
        cover: String?,
    ) : this(
        id = id,
        creation = creation,
        href = href,
        title = title,
        author = author,
        identifier = identifier,
        progression = progression,
        rawMediaType = mediaType.toString(),
        cover = cover,
    )

    val url: AbsoluteUrl?
        get() {
            val validHref = if (href.startsWith(prefix = "/")) "file://$href" else href
            return AbsoluteUrl(url = validHref)
        }

    /**
     * Helper property to parse the [rawMediaType] string into a strong-typed [MediaType] object.
     * This is not persisted in the database.
     */
    val mediaType: MediaType?
        get() =
            MediaType(string = rawMediaType)

    companion object {

        const val TABLE_NAME = "books"
        const val ID = "id"
        const val CREATION_DATE = "creation_date"
        const val HREF = "href"
        const val TITLE = "title"
        const val AUTHOR = "author"
        const val IDENTIFIER = "identifier"
        const val PROGRESSION = "progression"
        const val MEDIA_TYPE = "media_type"
        const val COVER = "cover"
    }
}
