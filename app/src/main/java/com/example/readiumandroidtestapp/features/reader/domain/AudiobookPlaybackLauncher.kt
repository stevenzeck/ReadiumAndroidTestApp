package com.example.readiumandroidtestapp.features.reader.domain

import com.example.readiumandroidtestapp.core.domain.repository.BookRepository
import org.readium.r2.shared.publication.Locator
import org.readium.r2.shared.util.mediatype.MediaType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudiobookPlaybackLauncher @Inject constructor(
    private val bookRepository: BookRepository,
    private val openPublicationUseCase: OpenPublicationUseCase,
    private val readerSessionFactory: ReaderSessionFactory,
    private val audioPlaybackManager: AudioPlaybackManager,
) {
    suspend fun launchPlayback(
        bookId: Long,
        chapterIndex: Int? = null,
    ): Result<Unit> {
        val currentBook = audioPlaybackManager.book.value
        val currentNavigator = audioPlaybackManager.navigator.value

        if (currentBook?.id == bookId && currentNavigator != null) {
            val publication = audioPlaybackManager.publication.value
            if (chapterIndex != null && publication != null) {
                val chapters = publication.tableOfContents.ifEmpty {
                    publication.readingOrder
                }
                val link = chapters.getOrNull(index = chapterIndex)
                if (link != null) {
                    val locator = Locator(
                        href = link.url(),
                        mediaType = link.mediaType ?: MediaType.BINARY,
                        title = link.title,
                    )
                    currentNavigator.go(locator = locator, animated = false)
                }
            }
            currentNavigator.play()
            return Result.success(value = Unit)
        }

        val book = bookRepository.get(bookId = bookId)
            ?: return Result.failure(exception = IllegalArgumentException("Book not found"))

        val url = book.url
            ?: return Result.failure(exception = IllegalArgumentException("Book URL is null"))

        val openedBookResult = openPublicationUseCase(url = url)
        val openedBook = openedBookResult.getOrElse { error ->
            return Result.failure(exception = error)
        }

        val audioSessionResult = readerSessionFactory.createAudioSession(
            book = book,
            publication = openedBook.publication,
        )

        val audioSession = audioSessionResult.getOrElse { error ->
            openedBook.asset.close()
            openedBook.publication.close()
            return Result.failure(exception = error)
        }

        if (chapterIndex != null) {
            val chapters = openedBook.publication.tableOfContents.ifEmpty {
                openedBook.publication.readingOrder
            }
            val link = chapters.getOrNull(index = chapterIndex)
            if (link != null) {
                val locator = Locator(
                    href = link.url(),
                    mediaType = link.mediaType ?: MediaType.BINARY,
                    title = link.title,
                )
                audioSession.navigator.go(locator = locator, animated = false)
            }
        }

        audioPlaybackManager.load(
            book = book,
            publication = openedBook.publication,
            asset = openedBook.asset,
            audioNavigator = audioSession.navigator,
            editor = audioSession.preferencesEditor,
        )

        audioSession.navigator.play()
        return Result.success(value = Unit)
    }
}
