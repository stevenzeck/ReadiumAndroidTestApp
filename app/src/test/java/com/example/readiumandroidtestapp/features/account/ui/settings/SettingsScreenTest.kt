package com.example.readiumandroidtestapp.features.account.ui.settings

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.readiumandroidtestapp.R
import com.example.readiumandroidtestapp.core.designsystem.theme.AppTheme
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val viewModel = mockk<SettingsViewModel>(relaxed = true)

    @Test
    fun displaysThemeOptions() {
        every { viewModel.appTheme } returns MutableStateFlow(value = AppTheme.SYSTEM)
        val context = ApplicationProvider.getApplicationContext<Context>()

        composeTestRule.setContent {
            SettingsScreen(viewModel = viewModel)
        }

        // Check title
        val settingsTitle = context.getString(R.string.settings)
        composeTestRule.onNodeWithText(text = settingsTitle).assertIsDisplayed()

        // Check options
        val systemDefault = context.getString(R.string.system_default)
        val light = context.getString(R.string.light)
        val dark = context.getString(R.string.dark)

        composeTestRule.onNodeWithText(text = systemDefault).assertIsDisplayed()
        composeTestRule.onNodeWithText(text = light).assertIsDisplayed()
        composeTestRule.onNodeWithText(text = dark).assertIsDisplayed()
    }

    @Test
    fun displaysSelectedTheme() {
        every { viewModel.appTheme } returns MutableStateFlow(value = AppTheme.DARK)
        val context = ApplicationProvider.getApplicationContext<Context>()

        composeTestRule.setContent {
            SettingsScreen(viewModel = viewModel)
        }

        context.getString(R.string.dark)
    }

    @Test
    fun clickingThemeTriggersViewModel() {
        every { viewModel.appTheme } returns MutableStateFlow(value = AppTheme.SYSTEM)
        val context = ApplicationProvider.getApplicationContext<Context>()

        composeTestRule.setContent {
            SettingsScreen(viewModel = viewModel)
        }

        val dark = context.getString(R.string.dark)
        composeTestRule.onNodeWithText(text = dark).performClick()

        verify { viewModel.setTheme(theme = AppTheme.DARK) }
    }
}
