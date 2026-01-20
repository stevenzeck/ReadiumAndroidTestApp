package com.example.readiumandroidtestapp.core.navigation

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation3.runtime.NavKey

/**
 * A CompositionLocal to provide the [Navigator] instance to the composition tree.
 *
 * This allows deeply nested composables to trigger navigation actions without
 * having to pass the navigator down through every layer of the hierarchy.
 */
val LocalNavigator = staticCompositionLocalOf<Navigator> {
    error("LocalNavigator not provided")
}

/**
 * Handles application navigation logic by manipulating the [NavigationState].
 *
 * This class abstracts the complexity of managing multiple back stacks (for tabbed navigation)
 * and provides a simple API for features to request navigation actions.
 *
 * @property state The underlying state holder that persists the back stacks and current route.
 */
class Navigator(val state: NavigationState) {

    /**
     * Navigates to a specific route.
     *
     * This method handles the logic for switching between top-level destinations (tabs)
     * versus pushing new screens onto the current stack.
     *
     * Logic:
     * 1. If the [route] is a known Top-Level Destination (e.g., Bookshelf, Catalogs),
     *    it switches the active tab to that destination.
     * 2. If the [route] is a regular screen (e.g., Reader, Detail), it is pushed onto the
     *    back stack of the currently active tab.
     *
     * @param route The target destination key.
     */
    fun navigate(route: NavKey) {
        if (route in state.backStacks.keys) {
            state.topLevelRoute = route
        } else {
            state.backStacks[state.topLevelRoute]?.add(element = route)
        }
    }

    /**
     * Handles the system Back event or the UI "Up" action.
     *
     * This method implements the standard Android back navigation behavior for a
     * multi-stack application (BottomNavigation style):
     *
     * 1. **Pop Stack**: If the current tab's stack has more than one entry (deep navigation),
     *    it pops the top entry.
     * 2. **Switch Tab**: If the current stack is at its root (the tab itself) and it is NOT
     *    the start destination, it switches back to the start tab (Bookshelf).
     * 3. **Exit App**: If the user is on the root of the start tab, it returns `false`,
     *    signaling the activity to handle the exit (e.g., `activity.finish()`).
     *
     * @return `true` if the back event was consumed by the Navigator, `false` otherwise.
     */
    fun goBack(): Boolean {
        val currentStack = state.backStacks[state.topLevelRoute]
            ?: error("Stack for ${state.topLevelRoute} not found")
        val currentRoute = currentStack.last()

        if (currentRoute == state.topLevelRoute) {
            if (state.topLevelRoute == state.startRoute) {
                return false
            }
            state.topLevelRoute = state.startRoute
            return true
        } else {
            currentStack.removeLastOrNull()
            return true
        }
    }
}
