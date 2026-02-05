package com.example.readiumandroidtestapp.main

import android.net.Uri
import com.example.readiumandroidtestapp.R
import com.example.readiumandroidtestapp.core.data.book.ImportError
import com.example.readiumandroidtestapp.core.designsystem.theme.AppTheme
import com.example.readiumandroidtestapp.core.domain.gateway.UrlGateway
import com.example.readiumandroidtestapp.core.domain.model.Book
import com.example.readiumandroidtestapp.core.domain.repository.BookRepository
import com.example.readiumandroidtestapp.core.domain.repository.SettingsRepository
import com.example.readiumandroidtestapp.core.utils.UserMessageManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.readium.r2.shared.util.AbsoluteUrl
import org.readium.r2.shared.util.Try

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    private val bookRepository: BookRepository = mockk()
    private val userMessageManager: UserMessageManager = mockk(relaxed = true)
    private val settingsRepository: SettingsRepository = mockk()
    private val urlGateway: UrlGateway = mockk()
    private val testDispatcher = StandardTestDispatcher()

    private lateinit var viewModel: MainViewModel

    private val appThemeFlow = MutableStateFlow(value = AppTheme.SYSTEM)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { settingsRepository.appTheme } returns appThemeFlow
        viewModel = MainViewModel(
            bookRepository = bookRepository,
            userMessageManager = userMessageManager,
            settingsRepository = settingsRepository,
            urlGateway = urlGateway,
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `appTheme reflects settingsRepository value`() = runTest {
        backgroundScope.launch { viewModel.appTheme.collect {} }

        assertEquals(AppTheme.SYSTEM, viewModel.appTheme.value)

        appThemeFlow.value = AppTheme.DARK
        advanceUntilIdle()
        assertEquals(AppTheme.DARK, viewModel.appTheme.value)
    }

    @Test
    fun `importBook with URI calls repository and emits success message`() = runTest {
        val uri = mockk<Uri>()
        val book = mockk<Book>()
        coEvery { bookRepository.addBook(uri = any()) } returns Try.success(success = book)

        viewModel.importBook(uri = uri)
        advanceUntilIdle()

        coVerify { bookRepository.addBook(uri = uri) }
        coVerify { userMessageManager.emitMessage(messageId = R.string.book_imported_successfully) }
    }

    @Test
    fun `importBook with URI emits error message on failure`() = runTest {
        val uri = mockk<Uri>()
        coEvery { bookRepository.addBook(uri = any()) } returns Try.failure(failure = ImportError.InvalidBook)

        viewModel.importBook(uri = uri)
        advanceUntilIdle()

        coVerify { userMessageManager.emitMessage(messageId = R.string.error_importing_book) }
    }

    @Test
    fun `importBook with valid URL calls repository`() = runTest {
        val urlString = "https://example.com/book.epub"
        val book = mockk<Book>()
        val absoluteUrl = mockk<AbsoluteUrl>()
        every { urlGateway.parseAbsoluteUrl(urlString) } returns absoluteUrl
        coEvery { bookRepository.addBook(url = any()) } returns Try.success(success = book)

        viewModel.importBook(url = urlString)
        advanceUntilIdle()

        coVerify { bookRepository.addBook(url = absoluteUrl) }
        coVerify { userMessageManager.emitMessage(messageId = R.string.book_imported_successfully) }
    }

    @Test
    fun `importBook with invalid URL emits error message`() = runTest {
        val urlString = ""
        every { urlGateway.parseAbsoluteUrl(urlString) } returns null

        viewModel.importBook(url = urlString)
        advanceUntilIdle()

        coVerify { userMessageManager.emitMessage(messageId = R.string.error_invalid_url) }
        coVerify(exactly = 0) { bookRepository.addBook(url = any()) }
    }
}
