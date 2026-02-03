package com.example.readiumandroidtestapp.core.designsystem.utils

import android.content.Context
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import com.example.readiumandroidtestapp.R
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class UiTextTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun `DynamicString asString(context) returns correct value`() {
        val text = "Hello"
        val uiText = UiText.DynamicString(text)
        Assert.assertEquals(text, uiText.asString(context))
    }

    @Test
    fun `DynamicString asString() composable returns correct value`() {
        val text = "Hello"
        val uiText = UiText.DynamicString(text)
        composeTestRule.setContent {
            Assert.assertEquals(text, uiText.asString())
        }
    }

    @Test
    fun `StringResource asString(context) returns correct value`() {
        val uiText = UiText.StringResource(R.string.app_name)
        val expected = context.getString(R.string.app_name)
        Assert.assertEquals(expected, uiText.asString(context))
    }

    @Test
    fun `StringResource asString() composable returns correct value`() {
        val uiText = UiText.StringResource(R.string.app_name)
        val expected = context.getString(R.string.app_name)
        composeTestRule.setContent {
            Assert.assertEquals(expected, uiText.asString())
        }
    }

    @Test
    fun `StringResource with arguments asString(context) returns correct value`() {
        val arg = "My Book"
        val uiText = UiText.StringResource(R.string.delete_book_message, listOf(arg))
        val expected = context.getString(R.string.delete_book_message, arg)
        Assert.assertEquals(expected, uiText.asString(context))
    }

    @Test
    fun `StringResource with arguments asString() composable returns correct value`() {
        val arg = "My Book"
        val uiText = UiText.StringResource(R.string.delete_book_message, listOf(arg))
        val expected = context.getString(R.string.delete_book_message, arg)
        composeTestRule.setContent {
            Assert.assertEquals(expected, uiText.asString())
        }
    }

    @Test
    fun `StringResource with nested UiText arguments asString(context) returns correct value`() {
        val nestedArg = UiText.DynamicString("Nested")
        val uiText = UiText.StringResource(R.string.delete_book_message, listOf(nestedArg))
        val expected = context.getString(R.string.delete_book_message, "Nested")
        Assert.assertEquals(expected, uiText.asString(context))
    }

    @Test
    fun `StringResource with nested UiText arguments asString() composable returns correct value`() {
        val nestedArg = UiText.DynamicString("Nested")
        val uiText = UiText.StringResource(R.string.delete_book_message, listOf(nestedArg))
        val expected = context.getString(R.string.delete_book_message, "Nested")
        composeTestRule.setContent {
            Assert.assertEquals(expected, uiText.asString())
        }
    }

    @Test
    fun `StringResource with multiple arguments asString(context) returns correct value`() {
        val arg1 = "Book Title"
        val uiText = UiText.StringResource(R.string.cover_description, listOf(arg1))
        val expected = context.getString(R.string.cover_description, arg1)
        Assert.assertEquals(expected, uiText.asString(context))
    }

    @Test
    fun `StringResource with nested StringResource asString(context) returns correct value`() {
        val nestedResource = UiText.StringResource(R.string.app_name)
        val uiText = UiText.StringResource(R.string.delete_book_message, listOf(nestedResource))

        val nestedExpected = context.getString(R.string.app_name)
        val expected = context.getString(R.string.delete_book_message, nestedExpected)

        Assert.assertEquals(expected, uiText.asString(context))
    }
}
