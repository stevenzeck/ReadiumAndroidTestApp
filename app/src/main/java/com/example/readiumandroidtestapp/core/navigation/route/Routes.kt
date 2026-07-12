package com.example.readiumandroidtestapp.core.navigation.route

import android.os.Parcelable
import androidx.navigation3.runtime.NavKey
import com.example.readiumandroidtestapp.core.domain.model.Catalog
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

/**
 * Type-safe route definitions for the application's navigation graph.
 *
 * This file defines the contract for navigation within the app.
 * All routes must implement [NavKey] to be recognized by the Navigation 3 library,
 * ensuring type safety and serialization support for state restoration.
 */
sealed interface Route : NavKey

/**
 * The root destination for the Bookshelf feature (Top-Level Destination).
 */
@Serializable
data object Bookshelf : Route

/**
 * The root destination for the Catalogs feature (Top-Level Destination).
 */
@Serializable
data object Catalogs : Route

/**
 * Sub-screens within the Catalogs feature.
 *
 * Note: These are implemented as a sealed class to group related screens,
 * but they do not strictly implement [Route] in this example unless they are also [NavKey]s.
 * (Assuming the app handles them as nested navigation, or they should implement NavKey if used directly).
 *
 * *Correction*: In Navigation 3, everything that is navigated to must be a [NavKey].
 * If these are used in `entry` calls, they act as keys.
 */
@Parcelize
sealed class CatalogScreens : Parcelable {
    data class CatalogDetail(val catalog: Catalog) : CatalogScreens()
    data class PublicationDetail(val manifestJson: String) : CatalogScreens()
}

/**
 * The destination for the Reader feature.
 *
 * This route triggers the full-screen reader experience.
 *
 * @param bookId The unique identifier of the book to open.
 */
@Serializable
data class Reader(val bookId: Long, val isAudiobook: Boolean = false) : Route

/**
 * The root destination for the Account feature (Top-Level Destination).
 */
@Serializable
data object Account : Route

/**
 * Sub-screens within the Account feature.
 */
@Parcelize
sealed class AccountScreens : Parcelable {
    data object Settings : AccountScreens()
    data object About : AccountScreens()
}
