package com.example.readiumandroidtestapp.main

import android.content.Context
import androidx.compose.material3.Button
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
import com.example.readiumandroidtestapp.core.designsystem.theme.AppTheme
import com.example.readiumandroidtestapp.core.domain.gateway.UrlGateway
import com.example.readiumandroidtestapp.core.domain.repository.BookRepository
import com.example.readiumandroidtestapp.core.domain.repository.SettingsRepository
import com.example.readiumandroidtestapp.core.navigation.LocalNavigator
import com.example.readiumandroidtestapp.core.navigation.route.Account
import com.example.readiumandroidtestapp.core.navigation.route.Bookshelf
import com.example.readiumandroidtestapp.core.navigation.route.Catalogs
import com.example.readiumandroidtestapp.core.navigation.route.NavEntryBuilder
import com.example.readiumandroidtestapp.core.navigation.route.Reader
import com.example.readiumandroidtestapp.core.utils.UserMessageManager
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
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

    private lateinit var viewModel: MainViewModel
    private val messagesFlow = MutableSharedFlow<Int>()
    private val appThemeFlow = MutableStateFlow(AppTheme.SYSTEM)

    @Before
    fun setup() {
        every { userMessageManager.messages } returns messagesFlow
        every { settingsRepository.appTheme } returns appThemeFlow

        viewModel = MainViewModel(
            bookRepository = bookRepository,
            userMessageManager = userMessageManager,
            settingsRepository = settingsRepository,
            urlGateway = urlGateway,
        )
    }

    private val testEntryBuilder: NavEntryBuilder = {
        entry<Bookshelf> {
            val navigator = LocalNavigator.current
            Button(onClick = { navigator.navigate(route = Reader(bookId = 1L)) }) {
                Text(text = "Go to Reader")
            }
            Text(text = "Bookshelf Content")
        }
        entry<Catalogs> {
            Text(text = "Catalogs Content")
        }
        entry<Account> {
            Text(text = "Account Content")
        }
        entry<Reader> {
            Text(text = "Reader Content")
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

    @Test
    fun `navigating to account displays account tab`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val accountTitle = context.getString(R.string.account)

        composeTestRule.setContent {
            CompositionLocalProvider(LocalViewModelStoreOwner provides viewModelStoreOwner) {
                ReadiumApp(
                    entryBuilders = setOf(testEntryBuilder),
                    viewModel = viewModel,
                )
            }
        }

        // Click on Account in bottom navigation
        composeTestRule.onNodeWithText(text = accountTitle).performClick()

        // Verify Account content is displayed
        composeTestRule.onNodeWithText(text = "Account Content").assertIsDisplayed()
    }

    @Test
    fun `navigating to reader hides bottom navigation`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val bookshelfTitle = context.getString(R.string.bookshelf)

        composeTestRule.setContent {
            CompositionLocalProvider(LocalViewModelStoreOwner provides viewModelStoreOwner) {
                ReadiumApp(
                    entryBuilders = setOf(testEntryBuilder),
                    viewModel = viewModel,
                )
            }
        }

        // Verify bottom bar is present
        composeTestRule.onNodeWithText(text = bookshelfTitle).assertIsDisplayed()

        // Click "Go to Reader" button
        composeTestRule.onNodeWithText(text = "Go to Reader").performClick()

        // Verify Reader content is displayed
        composeTestRule.onNodeWithText(text = "Reader Content").assertIsDisplayed()

        // Verify bottom bar item "Bookshelf" is no longer displayed (immersive mode)
        composeTestRule.onNodeWithText(text = bookshelfTitle).assertDoesNotExist()
    }

    @Test
    fun `user messages are displayed in snackbar`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val messageResId = R.string.book_imported_successfully
        val expectedMessage = context.getString(messageResId)

        composeTestRule.setContent {
            CompositionLocalProvider(LocalViewModelStoreOwner provides viewModelStoreOwner) {
                ReadiumApp(
                    entryBuilders = setOf(testEntryBuilder),
                    viewModel = viewModel,
                )
            }
        }

        // Emit a message
        messagesFlow.emit(messageResId)

        // Verify snackbar is displayed
        composeTestRule.onNodeWithText(text = expectedMessage).assertIsDisplayed()
    }
}
