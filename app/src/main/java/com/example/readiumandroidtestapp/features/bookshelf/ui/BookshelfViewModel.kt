package com.example.readiumandroidtestapp.features.bookshelf.ui

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.readiumandroidtestapp.R
import com.example.readiumandroidtestapp.core.domain.model.Book
import com.example.readiumandroidtestapp.core.domain.repository.BookRepository
import com.example.readiumandroidtestapp.core.utils.UserMessageManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel responsible for managing the state and user intents of the Bookshelf screen.
 *
 * This class coordinates:
 * - Observing the list of books from the repository.
 * - Handling user actions like deleting books.
 * - Emitting user-facing messages via [UserMessageManager].
 */
@HiltViewModel
class BookshelfViewModel @Inject constructor(
    private val bookRepository: BookRepository,
    private val userMessageManager: UserMessageManager,
) : ViewModel() {

    /**
     * The persistent stream of UI state.
     *
     * We use [SharingStarted.WhileSubscribed] with a 5-second timeout to allow the stream
     * to survive brief configuration changes (like screen rotation) without restarting
     * the upstream flows, optimizing performance.
     */
    val uiState: StateFlow<BookshelfUiState> = bookRepository.books
        .map { books ->
            if (books.isEmpty()) {
                BookshelfUiState.Empty
            } else {
                BookshelfUiState.Success(books = books)
            }
        }
        .catch { e ->
            Timber.e(e)
            emit(value = BookshelfUiState.Error)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
            initialValue = BookshelfUiState.Loading,
        )

    /**
     * Deletes a book by ID.
     *
     * ROP CONSUMER PATTERN:
     * This function calls [BookRepository.deleteBook], which returns a `Try` result.
     * Instead of try/catch blocks, we handle the `Failure` case functionally using `.onFailure`.
     * If the operation fails, we trigger a side effect (a Snackbar message) to inform the user.
     */
    fun deleteBook(bookId: Long) {
        viewModelScope.launch {
            bookRepository.deleteBook(bookId = bookId)
                .onFailure {
                    userMessageManager.emitMessage(messageId = R.string.error_deleting_book)
                }
        }
    }
}

/**
 * Represents the various UI states for the Bookshelf screen.
 */
@Immutable
sealed interface BookshelfUiState {
    data object Loading : BookshelfUiState

    @Immutable
    data class Success(val books: List<Book>) : BookshelfUiState

    data object Error : BookshelfUiState

    data object Empty : BookshelfUiState
}
