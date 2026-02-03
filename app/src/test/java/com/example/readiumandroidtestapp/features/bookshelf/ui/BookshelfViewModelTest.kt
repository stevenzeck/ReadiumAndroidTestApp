package com.example.readiumandroidtestapp.features.bookshelf.ui

import com.example.readiumandroidtestapp.R
import com.example.readiumandroidtestapp.core.domain.repository.BookRepository
import com.example.readiumandroidtestapp.core.domain.model.Book
import com.example.readiumandroidtestapp.core.utils.UserMessageManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.readium.r2.shared.util.Try

@OptIn(ExperimentalCoroutinesApi::class)
class BookshelfViewModelTest {

    private lateinit var viewModel: BookshelfViewModel
    private val bookRepository: BookRepository = mockk()
    private val userMessageManager: UserMessageManager = mockk(relaxed = true)
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
    fun `uiState emits Success when books exist`() = runTest(context = testDispatcher) {
        val books = listOf(mockk<Book>())
        every { bookRepository.books } returns MutableStateFlow(value = books)

        viewModel = BookshelfViewModel(
            bookRepository = bookRepository,
            userMessageManager = userMessageManager,
        )

        backgroundScope.launch(context = UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

        advanceUntilIdle()

        val state = viewModel.uiState.value
        Assert.assertTrue(state is BookshelfUiState.Success)
        Assert.assertEquals(books, (state as BookshelfUiState.Success).books)
    }

    @Test
    fun `uiState emits Empty when no books exist`() = runTest(context = testDispatcher) {
        every { bookRepository.books } returns MutableStateFlow(value = emptyList())

        viewModel = BookshelfViewModel(
            bookRepository = bookRepository,
            userMessageManager = userMessageManager,
        )

        backgroundScope.launch(context = UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

        advanceUntilIdle()

        val state = viewModel.uiState.value
        Assert.assertTrue(state is BookshelfUiState.Empty)
    }

    @Test
    fun `deleteBook success`() = runTest(context = testDispatcher) {
        every { bookRepository.books } returns MutableStateFlow(value = emptyList())
        coEvery { bookRepository.deleteBook(bookId = any()) } returns Try.Companion.success(success = Unit)

        viewModel = BookshelfViewModel(
            bookRepository = bookRepository,
            userMessageManager = userMessageManager,
        )

        viewModel.deleteBook(bookId = 1L)
        advanceUntilIdle()

        coVerify { bookRepository.deleteBook(bookId = 1L) }
        coVerify(exactly = 0) { userMessageManager.emitMessage(messageId = any()) }
    }

    @Test
    fun `deleteBook failure emits error message`() = runTest(context = testDispatcher) {
        every { bookRepository.books } returns MutableStateFlow(value = emptyList())
        coEvery { bookRepository.deleteBook(bookId = any()) } returns Try.Companion.failure(
            failure = Exception(
                "Error",
            ),
        )

        viewModel = BookshelfViewModel(
            bookRepository = bookRepository,
            userMessageManager = userMessageManager,
        )

        viewModel.deleteBook(bookId = 1L)
        advanceUntilIdle()

        coVerify { bookRepository.deleteBook(bookId = 1L) }
        coVerify { userMessageManager.emitMessage(messageId = R.string.error_deleting_book) }
    }
}
