package com.example.readiumandroidtestapp.core.domain.model

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey
import org.json.JSONObject
import org.readium.r2.shared.publication.Locator
import org.readium.r2.shared.util.Url
import org.readium.r2.shared.util.mediatype.MediaType

/**
 * Represents a user bookmark at a specific location in a book.
 *
 * @param id The unique ID of the bookmark.
 * @param creation The timestamp when the bookmark was created.
 * @param bookId Foreign key referencing the [Book] this bookmark belongs to.
 * @param resourceIndex The index of the resource (chapter/spine item) in the publication.
 * @param resourceHref The HREF of the resource.
 * @param resourceType The MIME type of the resource.
 * @param resourceTitle The title of the chapter/resource.
 * @param location A JSON string containing the precise Readium Locator data (progression, positioning) used to navigate back to this spot.
 * @param locatorText A JSON string containing the textual context (before, after, and highlighted text) used to display snippets in the UI.
 */
@Entity(
    tableName = Bookmark.TABLE_NAME,
    indices = [
        Index(
            value = [Bookmark.BOOK_ID, Bookmark.LOCATION],
            unique = true,
        ),
    ],
)
data class Bookmark(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = ID)
    var id: Long? = null,
    @ColumnInfo(name = CREATION_DATE)
    var creation: Long = System.currentTimeMillis(),
    @ColumnInfo(name = BOOK_ID)
    val bookId: Long,
    @ColumnInfo(name = RESOURCE_INDEX)
    val resourceIndex: Long,
    @ColumnInfo(name = RESOURCE_HREF)
    val resourceHref: String,
    @ColumnInfo(name = RESOURCE_TYPE)
    val resourceType: String,
    @ColumnInfo(name = RESOURCE_TITLE)
    val resourceTitle: String,
    @ColumnInfo(name = LOCATION)
    val location: String,
    @ColumnInfo(name = LOCATOR_TEXT)
    val locatorText: String,
) {

    val locator
        get() = Locator(
            href = Url(resourceHref)!!,
            mediaType = MediaType(resourceType) ?: MediaType.BINARY,
            title = resourceTitle,
            locations = Locator.Locations.fromJSON(JSONObject(location)),
            text = Locator.Text.fromJSON(JSONObject(locatorText)),
        )

    companion object {

        const val TABLE_NAME = "bookmarks"
        const val ID = "id"
        const val CREATION_DATE = "creation_date"
        const val BOOK_ID = "book_id"
        const val RESOURCE_INDEX = "resource_index"
        const val RESOURCE_HREF = "resource_href"
        const val RESOURCE_TYPE = "resource_type"
        const val RESOURCE_TITLE = "resource_title"
        const val LOCATION = "location"
        const val LOCATOR_TEXT = "locator_text"
    }
}
