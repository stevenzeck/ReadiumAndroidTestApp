package com.example.readiumandroidtestapp.features.account.ui

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.example.readiumandroidtestapp.R
import com.example.readiumandroidtestapp.core.navigation.api.AccountScreens
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AccountScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun accountListPane_displaysItems() {
        composeTestRule.setContent {
            AccountListPane(onItemClick = {})
        }

        val context = ApplicationProvider.getApplicationContext<Context>()
        val settingsText = context.getString(R.string.settings)
        val aboutText = context.getString(R.string.about)
        val titleText = context.getString(R.string.account)

        composeTestRule.onNodeWithText(text = titleText).assertIsDisplayed()
        composeTestRule.onNodeWithText(text = settingsText).assertIsDisplayed()
        composeTestRule.onNodeWithText(text = aboutText).assertIsDisplayed()
    }

    @Test
    fun accountListPane_clicksTriggerCallback() {
        var clickedScreen: AccountScreens? = null
        composeTestRule.setContent {
            AccountListPane(onItemClick = { clickedScreen = it })
        }

        val context = ApplicationProvider.getApplicationContext<Context>()
        val settingsText = context.getString(R.string.settings)
        val aboutText = context.getString(R.string.about)

        composeTestRule.onNodeWithText(text = settingsText).performClick()
        assertEquals(AccountScreens.Settings, clickedScreen)

        composeTestRule.onNodeWithText(text = aboutText).performClick()
        assertEquals(AccountScreens.About, clickedScreen)
    }
}
