package com.example.readiumandroidtestapp.features.catalogs.di

import com.example.readiumandroidtestapp.core.navigation.route.Catalogs
import com.example.readiumandroidtestapp.core.navigation.route.NavEntryBuilder
import com.example.readiumandroidtestapp.features.catalogs.ui.CatalogsScreen
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.multibindings.IntoSet

/**
 * Hilt module that plugs the Catalogs feature into the application's navigation graph.
 */
@Module
@InstallIn(ActivityRetainedComponent::class)
object CatalogsNavModule {

    /**
     * Contributes the [Catalogs] navigation entry to the global set.
     */
    @Provides
    @IntoSet
    fun provideCatalogsEntry(): NavEntryBuilder = {
        entry<Catalogs> {
            CatalogsScreen()
        }
    }
}
