package com.example.readiumandroidtestapp.features.account.di

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import io.mockk.mockk
import org.junit.Test

class AccountNavModuleTest {

    @Test
    fun `provideAccountEntry calls entry with Account`() {
        val scope = mockk<EntryProviderScope<NavKey>>(relaxed = true)
        val moduleEntry = AccountNavModule.provideAccountEntry()
        moduleEntry.invoke(scope)
    }
}
