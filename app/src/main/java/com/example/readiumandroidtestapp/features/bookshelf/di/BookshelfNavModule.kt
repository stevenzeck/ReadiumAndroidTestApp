package com.example.readiumandroidtestapp.features.bookshelf.di

import com.example.readiumandroidtestapp.core.navigation.LocalNavigator
import com.example.readiumandroidtestapp.core.navigation.api.Bookshelf
import com.example.readiumandroidtestapp.core.navigation.api.NavEntryBuilder
import com.example.readiumandroidtestapp.core.navigation.api.Reader
import com.example.readiumandroidtestapp.features.bookshelf.ui.BookshelfScreen
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.multibindings.IntoSet

/**
 * Hilt module that plugs the Bookshelf feature into the application's navigation graph.
 */
@Module
@InstallIn(ActivityRetainedComponent::class)
object BookshelfNavModule {

    /**
     * Contributes the [Bookshelf] navigation entry to the global set.
     *
     * This defines the UI content for the [Bookshelf] route and handles navigation events
     * (e.g., opening a book).
     */
    @Provides
    @IntoSet
    fun provideBookshelfEntry(): NavEntryBuilder = {
        entry<Bookshelf> {
            val navigator = LocalNavigator.current

            BookshelfScreen(
                onOpenBook = { bookId ->
                    navigator.navigate(route = Reader(bookId = bookId))
                },
            )
        }
    }
}
