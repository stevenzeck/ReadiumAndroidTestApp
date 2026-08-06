package com.example.readiumandroidtestapp.features.bookshelf.di

import androidx.media3.common.util.UnstableApi
import com.example.readiumandroidtestapp.core.navigation.LocalNavigator
import com.example.readiumandroidtestapp.core.navigation.route.Bookshelf
import com.example.readiumandroidtestapp.core.navigation.route.NavEntryBuilder
import com.example.readiumandroidtestapp.core.navigation.route.Reader
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
@UnstableApi
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
                onOpenBook = { book ->
                    navigator.navigate(
                        route = Reader(
                            bookId = book.id,
                            isAudiobook = book.rawMediaType.contains(
                                "audiobook",
                                ignoreCase = true,
                            ),
                        ),
                    )
                },
            )
        }
    }
}
