package com.example.readiumandroidtestapp.features.account.ui.settings

import com.example.readiumandroidtestapp.core.data.settings.SettingsRepository
import com.example.readiumandroidtestapp.core.ui.theme.AppTheme
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
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

    private lateinit var viewModel: SettingsViewModel
    private val settingsRepository: SettingsRepository = mockk(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher = testDispatcher)
        every { settingsRepository.appTheme } returns MutableStateFlow(value = AppTheme.SYSTEM)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state reflects repository`() = runTest(context = testDispatcher) {
        viewModel = SettingsViewModel(settingsRepository = settingsRepository)
        advanceUntilIdle()

        assertEquals(AppTheme.SYSTEM, viewModel.appTheme.value)
    }

    @Test
    fun `setTheme calls repository`() = runTest(context = testDispatcher) {
        viewModel = SettingsViewModel(settingsRepository = settingsRepository)
        advanceUntilIdle()

        viewModel.setTheme(theme = AppTheme.DARK)
        advanceUntilIdle()

        coVerify { settingsRepository.setAppTheme(theme = AppTheme.DARK) }
    }
}
