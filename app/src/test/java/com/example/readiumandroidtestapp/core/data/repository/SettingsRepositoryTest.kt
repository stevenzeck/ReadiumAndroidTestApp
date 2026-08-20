package com.example.readiumandroidtestapp.core.data.repository

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.readiumandroidtestapp.core.designsystem.theme.AppTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsRepositoryTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val repository = SettingsRepository(context = context)

    @Before
    fun setUp() = runTest {
        repository.setAppTheme(theme = AppTheme.SYSTEM)
    }

    @Test
    fun `appTheme emits SYSTEM by default`() = runTest {
        val theme = repository.appTheme.first()
        assertEquals("Expected SYSTEM but got $theme", AppTheme.SYSTEM, theme)
    }

    @Test
    fun `setAppTheme updates theme`() = runTest {
        repository.setAppTheme(theme = AppTheme.DARK)
        val theme = repository.appTheme.first()
        assertEquals(AppTheme.DARK, theme)
    }
}
