package com.example.readiumandroidtestapp.features.bookshelf.ui

import com.example.readiumandroidtestapp.R
import com.example.readiumandroidtestapp.core.data.repository.FakeBookRepository
import com.example.readiumandroidtestapp.core.domain.model.Book
import com.example.readiumandroidtestapp.core.domain.repository.BookRepository
import com.example.readiumandroidtestapp.core.utils.UserMessageManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
import org.readium.r2.shared.util.mediatype.MediaType

@OptIn(ExperimentalCoroutinesApi::class)
class BookshelfViewModelTest {

    private lateinit var viewModel: BookshelfViewModel
    private lateinit var fakeRepository: FakeBookRepository
    private val userMessageManager: UserMessageManager = mockk(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher = testDispatcher)
        fakeRepository = FakeBookRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `uiState emits Success when books exist`() = runTest(context = testDispatcher) {
        val books = listOf(
            Book(
                id = 1L,
                href = "path1",
                title = "Book 1",
                identifier = "id1",
                mediaType = MediaType.EPUB,
                cover = null,
            ),
            Book(
                id = 2L,
                href = "path2",
                title = "Book 2",
                identifier = "id2",
                mediaType = MediaType.EPUB,
                cover = null,
            ),
        )
        fakeRepository.addBooks(*books.toTypedArray())

        viewModel = BookshelfViewModel(
            bookRepository = fakeRepository,
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
        viewModel = BookshelfViewModel(
            bookRepository = fakeRepository,
            userMessageManager = userMessageManager,
        )

        backgroundScope.launch(context = UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

        advanceUntilIdle()

        Assert.assertTrue(viewModel.uiState.value is BookshelfUiState.Empty)
    }

    @Test
    fun `deleteBook removes book from uiState`() = runTest(testDispatcher) {
        val book = Book(
            id = 123L,
            href = "href",
            title = "Title",
            identifier = "id",
            mediaType = MediaType.EPUB,
            cover = null,
        )
        fakeRepository.addBooks(book)

        viewModel = BookshelfViewModel(
            bookRepository = fakeRepository,
            userMessageManager = userMessageManager,
        )

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        advanceUntilIdle()

        Assert.assertTrue(viewModel.uiState.value is BookshelfUiState.Success)

        viewModel.deleteBook(bookId = book.id)
        advanceUntilIdle()

        Assert.assertTrue(viewModel.uiState.value is BookshelfUiState.Empty)
    }

    @Test
    fun `deleteBook failure emits error message`() = runTest(testDispatcher) {
        val mockRepo = mockk<BookRepository>(relaxed = true)
        val book = Book(
            id = 123L,
            href = "href",
            title = "Title",
            identifier = "id",
            mediaType = MediaType.EPUB,
            cover = null,
        )

        coEvery { mockRepo.deleteBook(bookId = any()) } returns Try.failure(failure = Exception("Fail"))

        val viewModelWithMock =
            BookshelfViewModel(bookRepository = mockRepo, userMessageManager = userMessageManager)

        viewModelWithMock.deleteBook(bookId = book.id)
        advanceUntilIdle()

        coVerify { userMessageManager.emitMessage(messageId = R.string.error_deleting_book) }
    }
}
