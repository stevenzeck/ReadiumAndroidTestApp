package com.example.readiumandroidtestapp.features.account.ui.settings

import com.example.readiumandroidtestapp.core.data.settings.SettingsRepository
import com.example.readiumandroidtestapp.core.ui.theme.AppTheme
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val settingsRepository: SettingsRepository = mockk()
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher = testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `appTheme emits initial value from repository`() = runTest {
        val theme = AppTheme.DARK
        every { settingsRepository.appTheme } returns flowOf(value = theme)

        val viewModel = SettingsViewModel(
            settingsRepository = settingsRepository,
        )

        backgroundScope.launch(context = UnconfinedTestDispatcher(scheduler = testScheduler)) {
            viewModel.appTheme.collect {}
        }

        advanceUntilIdle()

        assertEquals(theme, viewModel.appTheme.value)
    }

    @Test
    fun `setTheme calls repository`() = runTest {
        val theme = AppTheme.LIGHT
        every { settingsRepository.appTheme } returns flowOf(value = AppTheme.SYSTEM)
        coEvery { settingsRepository.setAppTheme(theme = theme) } returns Unit

        val viewModel = SettingsViewModel(
            settingsRepository = settingsRepository,
        )

        viewModel.setTheme(theme = theme)

        advanceUntilIdle()

        coVerify { settingsRepository.setAppTheme(theme = theme) }
    }
}
