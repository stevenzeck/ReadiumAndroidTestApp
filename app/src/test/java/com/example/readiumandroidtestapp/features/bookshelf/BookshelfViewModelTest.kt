package com.example.readiumandroidtestapp.features.bookshelf

import com.example.readiumandroidtestapp.R
import com.example.readiumandroidtestapp.core.data.book.BookRepository
import com.example.readiumandroidtestapp.core.domain.model.Book
import com.example.readiumandroidtestapp.core.utils.UserMessageManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.readium.r2.shared.util.Try

@OptIn(ExperimentalCoroutinesApi::class)
class BookshelfViewModelTest {

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
    fun `uiState emits Loading initially`() = runTest {
        every { bookRepository.books } returns flowOf(value = emptyList())
        val viewModel = BookshelfViewModel(
            bookRepository = bookRepository,
            userMessageManager = userMessageManager,
        )

        assertEquals(BookshelfUiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun `uiState emits Empty when no books`() = runTest {
        every { bookRepository.books } returns flowOf(value = emptyList())
        val viewModel = BookshelfViewModel(
            bookRepository = bookRepository,
            userMessageManager = userMessageManager,
        )

        backgroundScope.launch(context = UnconfinedTestDispatcher(scheduler = testScheduler)) {
            viewModel.uiState.collect()
        }

        advanceUntilIdle()

        assertEquals(BookshelfUiState.Empty, viewModel.uiState.value)
    }

    @Test
    fun `uiState emits Success when books exist`() = runTest {
        val books = listOf(mockk<Book>())
        every { bookRepository.books } returns flowOf(value = books)
        val viewModel = BookshelfViewModel(
            bookRepository = bookRepository,
            userMessageManager = userMessageManager,
        )

        backgroundScope.launch(context = UnconfinedTestDispatcher(scheduler = testScheduler)) {
            viewModel.uiState.collect()
        }

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is BookshelfUiState.Success)
        assertEquals(books, (state as BookshelfUiState.Success).books)
    }

    @Test
    fun `uiState emits Error on exception`() = runTest {
        every { bookRepository.books } returns flow { throw RuntimeException("Error") }
        val viewModel = BookshelfViewModel(
            bookRepository = bookRepository,
            userMessageManager = userMessageManager,
        )

        backgroundScope.launch(context = UnconfinedTestDispatcher(scheduler = testScheduler)) {
            viewModel.uiState.collect()
        }

        advanceUntilIdle()

        assertEquals(BookshelfUiState.Error, viewModel.uiState.value)
    }

    @Test
    fun `deleteBook calls repository`() = runTest {
        val bookId = 1L
        every { bookRepository.books } returns flowOf(value = emptyList())
        coEvery { bookRepository.deleteBook(bookId = bookId) } returns Try.success(success = Unit)

        val viewModel = BookshelfViewModel(
            bookRepository = bookRepository,
            userMessageManager = userMessageManager,
        )
        viewModel.deleteBook(bookId = bookId)

        advanceUntilIdle()

        coVerify { bookRepository.deleteBook(bookId) }
    }

    @Test
    fun `deleteBook emits error message on failure`() = runTest {
        val bookId = 1L
        every { bookRepository.books } returns flowOf(value = emptyList())
        coEvery { bookRepository.deleteBook(bookId = bookId) } returns Try.failure(
            failure = Exception(
                "Failed",
            ),
        )

        val viewModel = BookshelfViewModel(
            bookRepository = bookRepository,
            userMessageManager = userMessageManager,
        )
        viewModel.deleteBook(bookId = bookId)

        advanceUntilIdle()

        coVerify { userMessageManager.emitMessage(messageId = R.string.error_deleting_book) }
    }
}
