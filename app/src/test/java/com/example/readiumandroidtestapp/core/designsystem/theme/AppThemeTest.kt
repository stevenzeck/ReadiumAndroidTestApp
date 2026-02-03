package com.example.readiumandroidtestapp.core.designsystem.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
class AppThemeTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun `uses light colors when dark theme is false`() {
        var surfaceColor: Color = Color.Companion.Unspecified
        val expected = lightColorScheme().surface

        composeTestRule.setContent {
            AppTheme(useDarkTheme = false) {
                surfaceColor = MaterialTheme.colorScheme.surface
            }
        }

        Assert.assertEquals(expected, surfaceColor)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun `uses dark colors when dark theme is true`() {
        var surfaceColor: Color = Color.Companion.Unspecified
        val expected = darkColorScheme().surface

        composeTestRule.setContent {
            AppTheme(useDarkTheme = true) {
                surfaceColor = MaterialTheme.colorScheme.surface
            }
        }

        Assert.assertEquals(expected, surfaceColor)
    }
}
