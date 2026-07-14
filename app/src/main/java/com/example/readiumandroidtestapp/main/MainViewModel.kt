package com.example.readiumandroidtestapp.main

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.readiumandroidtestapp.R
import com.example.readiumandroidtestapp.core.data.book.ImportError
import com.example.readiumandroidtestapp.core.designsystem.theme.AppTheme
import com.example.readiumandroidtestapp.core.domain.gateway.UrlGateway
import com.example.readiumandroidtestapp.core.domain.model.Book
import com.example.readiumandroidtestapp.core.domain.repository.BookRepository
import com.example.readiumandroidtestapp.core.domain.repository.SettingsRepository
import com.example.readiumandroidtestapp.core.utils.UserMessageManager
import com.example.readiumandroidtestapp.features.reader.domain.AudioPlaybackManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.readium.r2.shared.util.Try
import javax.inject.Inject

/**
 * Global ViewModel for the application shell.
 *
 * Responsibilities:
 * 1. **Global Message Handling**: Exposes `userMessages` to [ReadiumApp] for displaying Snackbars.
 * 2. **Book Import Logic**: Handles the logic for importing books from URIs or URLs, interacting with the [BookRepository].
 * 3. **App Settings**: Manages application-wide settings like Theme preference.
 *
 * @param bookRepository Repository for managing book data and imports.
 * @param userMessageManager Utility class for queuing user-facing messages.
 * @param settingsRepository Repository for persisting app preferences.
 * @param urlGateway Gateway for parsing URLs, enabling unit testing without Android/Readium dependencies.
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val bookRepository: BookRepository,
    private val userMessageManager: UserMessageManager,
    settingsRepository: SettingsRepository,
    private val urlGateway: UrlGateway,
    audioPlaybackManager: AudioPlaybackManager,
) : ViewModel() {

    /**
     * A flow of message resource IDs to be displayed to the user (e.g., via Snackbar).
     */
    val userMessages = userMessageManager.messages

    val activeAudioBook = audioPlaybackManager.book
    val audioNavigator = audioPlaybackManager.navigator
    val audioPreferencesEditor = audioPlaybackManager.preferencesEditor
    val expandPlayerEvent = audioPlaybackManager.expandPlayerEvent

    /**
     * The current application theme (Light, Dark, System).
     */
    val appTheme = settingsRepository.appTheme.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
        initialValue = AppTheme.SYSTEM,
    )

    /**
     * Initiates the import of a book from a local file URI.
     */
    fun importBook(uri: Uri) {
        viewModelScope.launch {
            val result = bookRepository.addBook(uri = uri)
            handleImportResult(result)
        }
    }

    /**
     * Initiates the import of a book from a remote URL.
     */
    fun importBook(url: String) {
        viewModelScope.launch {
            val absoluteUrl = urlGateway.parseAbsoluteUrl(url)
            if (absoluteUrl == null) {
                userMessageManager.emitMessage(messageId = R.string.error_invalid_url)
                return@launch
            }

            val result = bookRepository.addBook(url = absoluteUrl)
            handleImportResult(result)
        }
    }

    private suspend fun handleImportResult(result: Try<Book, ImportError>) {
        result
            .onSuccess {
                userMessageManager.emitMessage(messageId = R.string.book_imported_successfully)
            }
            .onFailure { error ->
                val messageId = when (error) {
                    is ImportError.Network -> R.string.error_importing_book
                    is ImportError.Storage -> R.string.error_importing_book
                    is ImportError.InvalidBook -> R.string.error_importing_book
                    is ImportError.Database -> R.string.error_importing_book
                    else -> R.string.error_importing_book
                }
                userMessageManager.emitMessage(messageId = messageId)
            }
    }
}
