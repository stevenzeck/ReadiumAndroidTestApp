package com.example.readiumandroidtestapp.app

import android.content.Context
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.test.core.app.ApplicationProvider
import com.example.readiumandroidtestapp.R
import com.example.readiumandroidtestapp.core.data.book.BookRepository
import com.example.readiumandroidtestapp.core.data.settings.SettingsRepository
import com.example.readiumandroidtestapp.core.domain.gateway.UrlGateway
import com.example.readiumandroidtestapp.core.navigation.api.Account
import com.example.readiumandroidtestapp.core.navigation.api.Bookshelf
import com.example.readiumandroidtestapp.core.navigation.api.Catalogs
import com.example.readiumandroidtestapp.core.navigation.api.NavEntryBuilder
import com.example.readiumandroidtestapp.core.ui.theme.AppTheme
import com.example.readiumandroidtestapp.core.utils.UserMessageManager
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ReadiumAppTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val bookRepository: BookRepository = mockk(relaxed = true)
    private val userMessageManager: UserMessageManager = mockk(relaxed = true)
    private val settingsRepository: SettingsRepository = mockk(relaxed = true)
    private val urlGateway: UrlGateway = mockk(relaxed = true)

    private lateinit var viewModel: AppViewModel
    private val messagesFlow = MutableSharedFlow<Int>()
    private val appThemeFlow = MutableStateFlow(AppTheme.SYSTEM)

    @Before
    fun setup() {
        every { userMessageManager.messages } returns messagesFlow
        every { settingsRepository.appTheme } returns appThemeFlow

        viewModel = AppViewModel(
            bookRepository = bookRepository,
            userMessageManager = userMessageManager,
            settingsRepository = settingsRepository,
            urlGateway = urlGateway,
        )
    }

    private val testEntryBuilder: NavEntryBuilder = {
        entry<Bookshelf> {
            Text(text = "Bookshelf Content")
        }
        entry<Catalogs> {
            Text(text = "Catalogs Content")
        }
        entry<Account> {
            Text(text = "Account Content")
        }
    }

    private val viewModelStoreOwner = object : ViewModelStoreOwner {
        override val viewModelStore = ViewModelStore()
    }

    @Test
    fun `initial state displays bookshelf`() {
        composeTestRule.setContent {
            CompositionLocalProvider(LocalViewModelStoreOwner provides viewModelStoreOwner) {
                ReadiumApp(
                    entryBuilders = setOf(testEntryBuilder),
                    viewModel = viewModel,
                )
            }
        }

        // Verify Bookshelf content is displayed
        composeTestRule.onNodeWithText(text = "Bookshelf Content").assertIsDisplayed()

        // Verify Bottom Bar Item "Bookshelf" is displayed
        val context = ApplicationProvider.getApplicationContext<Context>()
        val bookshelfTitle = context.getString(R.string.bookshelf)
        composeTestRule.onNodeWithText(text = bookshelfTitle).assertIsDisplayed()
    }

    @Test
    fun `navigating to catalogs displays catalogs tab`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val catalogsTitle = context.getString(R.string.catalogs)

        composeTestRule.setContent {
            CompositionLocalProvider(LocalViewModelStoreOwner provides viewModelStoreOwner) {
                ReadiumApp(
                    entryBuilders = setOf(testEntryBuilder),
                    viewModel = viewModel,
                )
            }
        }

        // Click on Catalogs in bottom navigation
        composeTestRule.onNodeWithText(text = catalogsTitle).performClick()

        // Verify Catalogs content is displayed
        composeTestRule.onNodeWithText(text = "Catalogs Content").assertIsDisplayed()
    }
}
