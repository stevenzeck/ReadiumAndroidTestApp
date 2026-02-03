package com.example.readiumandroidtestapp.features.account.di

import com.example.readiumandroidtestapp.core.navigation.route.Account
import com.example.readiumandroidtestapp.core.navigation.route.NavEntryBuilder
import com.example.readiumandroidtestapp.features.account.ui.AccountScreen
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.multibindings.IntoSet

/**
 * Hilt module that plugs the Account feature into the application's navigation graph.
 */
@Module
@InstallIn(ActivityRetainedComponent::class)
object AccountNavModule {

    /**
     * Contributes the [Account] navigation entry to the global set.
     */
    @Provides
    @IntoSet
    fun provideAccountEntry(): NavEntryBuilder = {
        entry<Account> {
            AccountScreen()
        }
    }
}
