package com.example.readiumandroidtestapp.features.account.ui

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.readiumandroidtestapp.R
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AboutInfoScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun aboutScreen_displaysContent() {
        composeTestRule.setContent {
            AboutInfoScreen()
        }

        val context = ApplicationProvider.getApplicationContext<Context>()
        val expectedVersion = context.getString(R.string.readium_version)
        val expectedTitle = context.getString(R.string.about)

        composeTestRule.onNodeWithText(text = expectedTitle).assertIsDisplayed()
        composeTestRule.onNodeWithText(text = expectedVersion).assertIsDisplayed()
    }
}
