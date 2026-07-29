package com.example.readiumandroidtestapp.features.reader.ui.audio

import android.net.Uri
import androidx.annotation.OptIn
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaLibraryService.MediaLibrarySession
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionError
import com.example.readiumandroidtestapp.core.domain.repository.BookRepository
import com.example.readiumandroidtestapp.features.reader.domain.AudioPlaybackManager
import com.example.readiumandroidtestapp.features.reader.domain.AudiobookPlaybackLauncher
import com.example.readiumandroidtestapp.features.reader.domain.OpenPublicationUseCase
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.SettableFuture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(UnstableApi::class)
class AudiobookLibrarySessionCallback @Inject constructor(
    private val bookRepository: BookRepository,
    private val openPublicationUseCase: OpenPublicationUseCase,
    private val audioPlaybackManager: AudioPlaybackManager,
    private val playbackLauncher: AudiobookPlaybackLauncher,
) : MediaLibrarySession.Callback {

    private val serviceScope = CoroutineScope(context = Dispatchers.Main)

    companion object {
        const val ROOT_ID = "ROOT_AUDIOBOOKS"
    }

    private fun <T> future(block: suspend () -> T): ListenableFuture<T> {
        val future = SettableFuture.create<T>()
        serviceScope.launch {
            try {
                future.set(block())
            } catch (e: Throwable) {
                future.setException(e)
            }
        }
        return future
    }

    override fun onGetLibraryRoot(
        session: MediaLibrarySession,
        browser: MediaSession.ControllerInfo,
        params: MediaLibraryService.LibraryParams?,
    ): ListenableFuture<LibraryResult<MediaItem>> {
        val rootItem = MediaItem.Builder()
            .setMediaId(ROOT_ID)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle("Audiobooks")
                    .setIsBrowsable(true)
                    .setIsPlayable(false)
                    .setMediaType(MediaMetadata.MEDIA_TYPE_FOLDER_MIXED)
                    .build(),
            )
            .build()
        return Futures.immediateFuture(LibraryResult.ofItem(rootItem, params))
    }

    override fun onGetChildren(
        session: MediaLibrarySession,
        browser: MediaSession.ControllerInfo,
        parentId: String,
        page: Int,
        pageSize: Int,
        params: MediaLibraryService.LibraryParams?,
    ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
        return future {
            if (parentId == ROOT_ID) {
                val books = bookRepository.books.first()
                val audiobooks = books.filter { it.rawMediaType.contains(other = "audiobook") }
                val items = audiobooks.map { book ->
                    val artworkUri: Uri? = book.cover?.let { coverPath ->
                        if (coverPath.startsWith(prefix = "/")) "file://$coverPath".toUri() else coverPath.toUri()
                    }
                    MediaItem.Builder()
                        .setMediaId("book_${book.id}")
                        .setMediaMetadata(
                            MediaMetadata.Builder()
                                .setTitle(book.title ?: "Unknown Title")
                                .setArtist(book.author ?: "Unknown Author")
                                .setIsBrowsable(true)
                                .setIsPlayable(true)
                                .setArtworkUri(artworkUri)
                                .setMediaType(MediaMetadata.MEDIA_TYPE_AUDIO_BOOK)
                                .build(),
                        )
                        .build()
                }
                LibraryResult.ofItemList(ImmutableList.copyOf(items), params)
            } else if (parentId.startsWith(prefix = "book_")) {
                val bookId = parentId.removePrefix(prefix = "book_").toLongOrNull()
                    ?: return@future LibraryResult.ofError(SessionError.ERROR_BAD_VALUE)
                val book = bookRepository.get(bookId = bookId)
                    ?: return@future LibraryResult.ofError(SessionError.ERROR_BAD_VALUE)

                val artworkUri: Uri? = book.cover?.let { coverPath ->
                    if (coverPath.startsWith(prefix = "/")) "file://$coverPath".toUri() else coverPath.toUri()
                }

                val activeBook = audioPlaybackManager.book.value
                val activePublication = audioPlaybackManager.publication.value

                val publication = if (activeBook?.id == bookId && activePublication != null) {
                    activePublication
                } else {
                    val url = book.url ?: return@future LibraryResult.ofError(SessionError.ERROR_BAD_VALUE)
                    openPublicationUseCase(url = url).getOrNull()?.publication
                        ?: return@future LibraryResult.ofError(SessionError.ERROR_BAD_VALUE)
                }

                val chapters = publication.tableOfContents.ifEmpty {
                    publication.readingOrder
                }

                val items = if (chapters.size > 1) {
                    chapters.mapIndexed { index, link ->
                        val chapterTitle = link.title ?: "Chapter ${index + 1}"
                        MediaItem.Builder()
                            .setMediaId("book_${bookId}_chapter_$index")
                            .setMediaMetadata(
                                MediaMetadata.Builder()
                                    .setTitle(chapterTitle)
                                    .setAlbumTitle(book.title ?: "Unknown Title")
                                    .setArtist(book.author ?: "Unknown Author")
                                    .setTrackNumber(index + 1)
                                    .setIsBrowsable(false)
                                    .setIsPlayable(true)
                                    .setArtworkUri(artworkUri)
                                    .setMediaType(MediaMetadata.MEDIA_TYPE_AUDIO_BOOK_CHAPTER)
                                    .build(),
                            )
                            .build()
                    }
                } else {
                    listOf(
                        MediaItem.Builder()
                            .setMediaId("book_${bookId}_chapter_0")
                            .setMediaMetadata(
                                MediaMetadata.Builder()
                                    .setTitle(book.title ?: "Unknown Title")
                                    .setArtist(book.author ?: "Unknown Author")
                                    .setIsBrowsable(false)
                                    .setIsPlayable(true)
                                    .setArtworkUri(artworkUri)
                                    .setMediaType(MediaMetadata.MEDIA_TYPE_AUDIO_BOOK_CHAPTER)
                                    .build(),
                            )
                            .build(),
                    )
                }

                LibraryResult.ofItemList(ImmutableList.copyOf(items), params)
            } else {
                LibraryResult.ofError(SessionError.ERROR_BAD_VALUE)
            }
        }
    }

    override fun onGetItem(
        session: MediaLibrarySession,
        browser: MediaSession.ControllerInfo,
        mediaId: String,
    ): ListenableFuture<LibraryResult<MediaItem>> {
        return future {
            if (mediaId == ROOT_ID) {
                val rootItem = MediaItem.Builder()
                    .setMediaId(ROOT_ID)
                    .setMediaMetadata(
                        MediaMetadata.Builder()
                            .setTitle("Audiobooks")
                            .setIsBrowsable(true)
                            .setIsPlayable(false)
                            .setMediaType(MediaMetadata.MEDIA_TYPE_FOLDER_MIXED)
                            .build(),
                    )
                    .build()
                LibraryResult.ofItem(rootItem, null)
            } else if (mediaId.startsWith(prefix = "book_")) {
                val bookId = if (mediaId.contains(other = "_chapter_")) {
                    mediaId.substringAfter(delimiter = "book_").substringBefore(delimiter = "_chapter_").toLongOrNull()
                } else {
                    mediaId.substringAfter(delimiter = "book_").toLongOrNull()
                }
                if (bookId == null) {
                    return@future LibraryResult.ofError(SessionError.ERROR_BAD_VALUE)
                }
                val book = bookRepository.get(bookId = bookId)
                    ?: return@future LibraryResult.ofError(SessionError.ERROR_BAD_VALUE)
                val artworkUri: Uri? = book.cover?.let { coverPath ->
                    if (coverPath.startsWith(prefix = "/")) "file://$coverPath".toUri() else coverPath.toUri()
                }
                val item = MediaItem.Builder()
                    .setMediaId(mediaId)
                    .setMediaMetadata(
                        MediaMetadata.Builder()
                            .setTitle(book.title ?: "Unknown Title")
                            .setArtist(book.author ?: "Unknown Author")
                            .setArtworkUri(artworkUri)
                            .setIsPlayable(true)
                            .build(),
                    )
                    .build()
                LibraryResult.ofItem(item, null)
            } else {
                LibraryResult.ofError(SessionError.ERROR_BAD_VALUE)
            }
        }
    }

    override fun onAddMediaItems(
        mediaSession: MediaSession,
        controller: MediaSession.ControllerInfo,
        mediaItems: List<MediaItem>,
    ): ListenableFuture<List<MediaItem>> {
        val firstItem = mediaItems.firstOrNull() ?: return Futures.immediateFuture(emptyList())
        val mediaId = firstItem.mediaId

        if (mediaId.startsWith(prefix = "book_")) {
            val bookId: Long?
            val chapterIndex: Int?
            if (mediaId.contains(other = "_chapter_")) {
                bookId = mediaId.substringAfter(delimiter = "book_").substringBefore(delimiter = "_chapter_").toLongOrNull()
                chapterIndex = mediaId.substringAfter(delimiter = "_chapter_").toIntOrNull()
            } else {
                bookId = mediaId.substringAfter(delimiter = "book_").toLongOrNull()
                chapterIndex = null
            }

            if (bookId != null) {
                serviceScope.launch {
                    playbackLauncher.launchPlayback(bookId = bookId, chapterIndex = chapterIndex)
                }
            }
        }

        return Futures.immediateFuture(mediaItems)
    }
}
