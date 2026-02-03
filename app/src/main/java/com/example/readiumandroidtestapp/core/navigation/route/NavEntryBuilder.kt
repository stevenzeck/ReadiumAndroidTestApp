package com.example.readiumandroidtestapp.core.navigation.route

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey

/**
 * A DSL typealias for defining navigation entries.
 *
 * This alias simplifies the function signature for contributing entries to the global navigation graph.
 * It corresponds to the lambda block used within Navigation 3's `entryProvider` DSL.
 *
 * Example Usage:
 * ```
 * val myEntry: NavEntryBuilder = {
 *     entry<MyRoute> { ... }
 * }
 * ```
 *
 * This is the core component of the "Plug-in Architecture", allowing features to provide
 * their own navigation logic without the main app needing to know the details.
 */
typealias NavEntryBuilder = EntryProviderScope<NavKey>.() -> Unit
