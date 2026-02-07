package com.example.readiumandroidtestapp.features.bookshelf.ui

import com.example.readiumandroidtestapp.R
import com.example.readiumandroidtestapp.core.domain.model.Book
import com.example.readiumandroidtestapp.core.domain.repository.BookRepository
import com.example.readiumandroidtestapp.core.utils.UserMessageManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
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
import org.readium.r2.shared.util.mediatype.MediaType

@OptIn(ExperimentalCoroutinesApi::class)
class BookshelfViewModelTest {

    private lateinit var viewModel: BookshelfViewModel
    private val bookRepository: BookRepository = mockk(relaxed = true)
    private val userMessageManager: UserMessageManager = mockk(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher = testDispatcher)
        viewModel = BookshelfViewModel(
            bookRepository = bookRepository,
            userMessageManager = userMessageManager,
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `uiState emits Success when books exist`() = runTest(context = testDispatcher) {
        val book1 = Book(
            id = 1L,
            href = "path/book1.epub",
            title = "Book 1",
            identifier = "id1",
            mediaType = MediaType(string = "application/epub+zip")!!,
            cover = null,
        )
        val book2 = Book(
            id = 2L,
            href = "path/book2.epub",
            title = "Book 2",
            identifier = "id2",
            mediaType = MediaType(string = "application/epub+zip")!!,
            cover = null,
        )
        val books = listOf(book1, book2)

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
    fun `deleteBook calls repository`() = runTest(testDispatcher) {
        // Given
        val bookId = 123L
        coEvery { bookRepository.deleteBook(bookId = bookId) } returns Try.success(success = Unit)

        // When
        viewModel.deleteBook(bookId = bookId)
        advanceUntilIdle()

        // Then
        coVerify { bookRepository.deleteBook(bookId = bookId) }
    }

    @Test
    fun `deleteBook failure emits error message`() = runTest(testDispatcher) {
        // Given
        val bookId = 123L
        coEvery { bookRepository.deleteBook(bookId = bookId) } returns Try.failure(
            failure = Exception("Fail"),
        )

        // When
        viewModel.deleteBook(bookId = bookId)
        advanceUntilIdle()

        // Then
        coVerify { userMessageManager.emitMessage(R.string.error_deleting_book) }
    }

    @Test
    fun `uiState emits Error when repository flow throws exception`() = runTest(testDispatcher) {
        // Given
        every { bookRepository.books } returns flow { throw Exception("Crash") }
        val brokenViewModel = BookshelfViewModel(
            bookRepository = bookRepository,
            userMessageManager = userMessageManager,
        )

        // When
        val states = mutableListOf<BookshelfUiState>()
        backgroundScope.launch(UnconfinedTestDispatcher(scheduler = testScheduler)) {
            brokenViewModel.uiState.collect { states.add(it) }
        }
        advanceUntilIdle()

        // Then
        Assert.assertTrue(states.last() is BookshelfUiState.Error)
    }
}
