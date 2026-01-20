package com.example.readiumandroidtestapp.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSerializable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.runtime.serialization.NavKeySerializer
import androidx.savedstate.compose.serialization.serializers.MutableStateSerializer

/**
 * Creates and remembers a [NavigationState] that persists configuration changes and process death.
 *
 * This function initializes the back stacks for all top-level routes and restores the current
 * top-level route from saved state if available.
 *
 * @param startRoute The initial route to display when the app launches (e.g., Bookshelf).
 * @param topLevelRoutes A list of all roots that define the main navigation tabs.
 * @return A remembered [NavigationState] instance.
 */
@Composable
fun rememberNavigationState(
    startRoute: NavKey,
    topLevelRoutes: List<NavKey>,
): NavigationState {
    val topLevelRoute = rememberSerializable(
        startRoute,
        topLevelRoutes,
        serializer = MutableStateSerializer(NavKeySerializer()),
    ) {
        mutableStateOf(startRoute)
    }

    val backStacks = topLevelRoutes.associateWith { key -> rememberNavBackStack(key) }

    return remember(startRoute, topLevelRoutes) {
        NavigationState(
            startRoute = startRoute,
            topLevelDestinations = topLevelRoutes,
            topLevelRoute = topLevelRoute,
            backStacks = backStacks,
        )
    }
}

/**
 * State holder for the application's navigation.
 *
 * It manages multiple back stacks (one for each top-level destination) to support
 * independent history for each tab (Navigation 3 style).
 *
 * @property startRoute The initial top-level route (usually the Bookshelf). The user will exit the app through this route.
 * @property topLevelDestinations A list of all available top-level roots (tabs).
 * @property topLevelRoute A mutable state holding the currently active top-level route.
 * @property backStacks A map associating each top-level route with its own [NavBackStack].
 */
class NavigationState(
    val startRoute: NavKey,
    val topLevelDestinations: List<NavKey>,
    topLevelRoute: MutableState<NavKey>,
    val backStacks: Map<NavKey, NavBackStack<NavKey>>,
) {
    var topLevelRoute: NavKey by topLevelRoute

    /**
     * Determines which back stacks should be currently active/rendered.
     *
     * **Keep-Alive Optimization:**
     * We always include the [startRoute] (Bookshelf) in this list.
     *
     * *Why?*
     * This ensures that the Bookshelf remains in the composition tree even when the user
     * switches to other tabs (like Catalogs). This preserves heavy state (like grid scroll position
     * and cover images), preventing expensive recompositions or data reloads when the user tabs back.
     *
     * If the user is on another tab, that tab is effectively drawn "over" the start route
     * (controlled by z-index or visibility in the UI layer).
     */
    val stacksInUse: List<NavKey>
        get() = if (topLevelRoute == startRoute) {
            listOf(startRoute)
        } else {
            listOf(startRoute, topLevelRoute)
        }

    /**
     * Returns the current active destination within the active top-level stack.
     * Used to determine UI state, such as which navigation item to highlight.
     */
    val currentDestination: NavKey
        get() = backStacks[topLevelRoute]?.last() ?: topLevelRoute
}

/**
 * Converts the abstract [NavigationState] into a list of renderable [NavEntry] objects.
 *
 * This function applies essential decorators to each entry to integrate with Android's lifecycle and saving mechanisms:
 * - [rememberSaveableStateHolderNavEntryDecorator]: Ensures UI state (like scroll position) is saved/restored.
 * - [rememberViewModelStoreNavEntryDecorator]: Scopes ViewModels to the navigation entry, ensuring they are cleared when the entry is popped.
 *
 * @param entryProvider The function that provides the content (Composable) for a given [NavKey].
 * @return A list of decorated [NavEntry]s ready for the [androidx.navigation3.ui.NavDisplay].
 */
@Composable
fun NavigationState.toEntries(
    entryProvider: (NavKey) -> NavEntry<NavKey>,
): SnapshotStateList<NavEntry<NavKey>> {
    val decoratedEntries = backStacks.mapValues { (_, stack) ->
        val decorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator<NavKey>(),
            rememberViewModelStoreNavEntryDecorator(),
        )
        rememberDecoratedNavEntries(
            backStack = stack,
            entryDecorators = decorators,
            entryProvider = entryProvider,
        )
    }

    return stacksInUse
        .flatMap { decoratedEntries[it] ?: emptyList() }
        .toMutableStateList()
}
