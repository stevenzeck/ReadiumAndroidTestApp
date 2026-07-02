package com.example.readiumandroidtestapp.core.domain.model

import android.os.Parcelable
import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

/**
 * Represents a grouping of publications.
 *
 * This entity is primarily used to store external OPDS feed configurations, allowing
 * users to browse remote libraries.
 *
 * @param id The unique ID of the catalog.
 * @param title The display title of the catalog.
 * @param href The URL of the catalog (e.g., the OPDS feed URL).
 * @param type Integer flag distinguishing the type of catalog.
 *             - 1: OPDS 1.x (XML).
 *             - 2: OPDS 2.x (JSON).
 */
@Serializable
@Parcelize
@Entity(tableName = Catalog.TABLE_NAME)
data class Catalog(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = ID)
    val id: Long? = null,
    @ColumnInfo(name = TITLE)
    val title: String,
    @ColumnInfo(name = HREF)
    val href: String,
    @ColumnInfo(name = TYPE)
    val type: Int,
) : Parcelable {
    companion object {

        const val TABLE_NAME = "catalogs"
        const val ID = "id"
        const val TITLE = "title"
        const val HREF = "href"
        const val TYPE = "type"
        const val TYPE_OPDS_1 = 1
        const val TYPE_OPDS_2 = 2
    }
}
