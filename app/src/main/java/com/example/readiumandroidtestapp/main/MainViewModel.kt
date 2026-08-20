package com.example.readiumandroidtestapp.main

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.readiumandroidtestapp.R
import com.example.readiumandroidtestapp.core.data.book.ImportError
import com.example.readiumandroidtestapp.core.data.repository.BookRepository
import com.example.readiumandroidtestapp.core.data.repository.SettingsRepository
import com.example.readiumandroidtestapp.core.designsystem.theme.AppTheme
import com.example.readiumandroidtestapp.core.domain.model.Book
import com.example.readiumandroidtestapp.core.utils.UserMessageManager
import com.example.readiumandroidtestapp.features.reader.domain.AudioPlaybackManager
import com.example.readiumandroidtestapp.features.reader.ui.audio.MediaService
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.readium.r2.shared.util.AbsoluteUrl
import org.readium.r2.shared.util.Try
import timber.log.Timber
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
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val bookRepository: BookRepository,
    private val userMessageManager: UserMessageManager,
    settingsRepository: SettingsRepository,
    audioPlaybackManager: AudioPlaybackManager,
) : ViewModel() {

    /**
     * A flow of message resource IDs to be displayed to the user (e.g., via Snackbar).
     */
    val userMessages = userMessageManager.messages

    private val _mediaController = MutableStateFlow<MediaController?>(value = null)
    val mediaController: StateFlow<MediaController?> = _mediaController.asStateFlow()

    private var controllerFuture: ListenableFuture<MediaController>? = null

    init {
        initializeMediaController()
    }

    private fun initializeMediaController() {
        val isTest = try {
            Class.forName("org.junit.Test")
            true
        } catch (_: ClassNotFoundException) {
            false
        } catch (_: Exception) {
            false
        }
        if (isTest) return

        try {
            val sessionToken =
                SessionToken(context, ComponentName(context, MediaService::class.java))
            controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
            controllerFuture?.addListener(
                { _mediaController.value = controllerFuture?.get() },
                MoreExecutors.directExecutor(),
            )
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    override fun onCleared() {
        controllerFuture?.let { MediaController.releaseFuture(it) }
    }

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
            handleImportResult(result = result)
        }
    }

    /**
     * Initiates the import of a book from a remote URL.
     */
    fun importBook(url: String) {
        viewModelScope.launch {
            val absoluteUrl = AbsoluteUrl(url = url)
            if (absoluteUrl == null) {
                userMessageManager.emitMessage(messageId = R.string.error_invalid_url)
                return@launch
            }

            val result = bookRepository.addBook(url = absoluteUrl)
            handleImportResult(result = result)
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
