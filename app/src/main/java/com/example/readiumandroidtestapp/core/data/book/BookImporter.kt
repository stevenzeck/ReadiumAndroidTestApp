package com.example.readiumandroidtestapp.core.data.book

import android.net.Uri
import com.example.readiumandroidtestapp.core.data.database.BooksDao
import com.example.readiumandroidtestapp.core.data.di.IoDispatcher
import com.example.readiumandroidtestapp.core.data.storage.StorageManager
import com.example.readiumandroidtestapp.core.domain.model.Book
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.readium.r2.shared.publication.Publication
import org.readium.r2.shared.util.AbsoluteUrl
import org.readium.r2.shared.util.Try
import org.readium.r2.shared.util.asset.Asset
import org.readium.r2.shared.util.asset.AssetRetriever
import org.readium.r2.shared.util.flatMap
import org.readium.r2.shared.util.http.HttpClient
import org.readium.r2.shared.util.http.HttpRequest
import org.readium.r2.shared.util.http.fetch
import org.readium.r2.streamer.PublicationOpener
import timber.log.Timber
import java.io.ByteArrayInputStream
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

sealed class ImportError {
    data object Network : ImportError()
    data object Storage : ImportError()
    data object InvalidBook : ImportError()
    data class Database(val cause: Exception) : ImportError()
    data class Unknown(val cause: Exception) : ImportError()
}

/**
 * Orchestrates the import of books into the application.
 *
 * This class handles the entire pipeline of:
 * 1. Fetching the content (from network or local URI).
 * 2. Saving the content to the app's internal storage (filesDir).
 * 3. Opening the saved file with Readium to parse metadata and extract the cover.
 * 4. Persisting the book metadata to the database.
 */
@Singleton
class BookImporter @Inject constructor(
    private val storageManager: StorageManager,
    private val booksDao: BooksDao,
    private val assetRetriever: AssetRetriever,
    private val publicationOpener: PublicationOpener,
    private val httpClient: HttpClient,
    private val coverImageSaver: CoverImageSaver,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {

    /**
     * Imports a book from a remote URL.
     *
     * We download the file to disk first because Readium parsers work best with seekable local files,
     * and we need to persist the book for offline access anyway.
     */
    suspend fun importFromUrl(url: AbsoluteUrl): Try<Book, ImportError> =
        withContext(context = ioDispatcher) {
            httpClient.fetch(request = HttpRequest(url = url))
                .mapFailure { ImportError.Network }
                .flatMap { response ->
                    val stream = ByteArrayInputStream(response.body)
                    var extension = url.extension?.toString()

                    if (extension.isNullOrBlank()) {
                        val contentType = response.response.headers["Content-Type"]
                            ?: response.response.headers["content-type"]
                        val mimeType = contentType?.firstOrNull()?.substringBefore(";")?.trim()

                        if (mimeType != null) {
                            extension =
                                storageManager.resolveExtensionFromMimeType(mimeType = mimeType)
                        }
                    }

                    // Save to local storage first to ensure we have a file handle for the parser.
                    storageManager.saveFileFromStream(input = stream, extension = extension)
                        .mapFailure { mapIOError(e = it) }
                }
                .flatMap { file ->
                    addBookFromFile(file = file)
                }
                .onFailure { error ->
                    Timber.e(message = "Error adding book from URL: $error")
                }
        }

    /**
     * Imports a book from a user-selected URI (e.g., SAF).
     *
     * Similar to URL import, we copy the stream to our internal private storage immediately.
     * This grants us full ownership of the file and avoids permission persistence issues with SAF URIs.
     */
    suspend fun importFromUri(uri: Uri): Try<Book, ImportError> =
        withContext(context = ioDispatcher) {
            try {
                val inputStream = storageManager.openInputStream(uri = uri)
                    ?: throw IOException("Could not open input stream")
                inputStream.use { stream ->
                    storageManager.saveFileFromStream(
                        input = stream,
                        extension = storageManager.resolveExtension(uri = uri),
                    )
                        .mapFailure { mapIOError(e = it) }
                }
            } catch (e: Exception) {
                Try.failure(failure = mapIOError(e = e))
            }
                .flatMap { file ->
                    addBookFromFile(file = file)
                }
                .onFailure { error ->
                    Timber.e(message = "Error adding book from URI: $error")
                }
        }

    private fun mapIOError(e: Exception): ImportError {
        Timber.e(t = e, message = "Storage error during import")
        return when (e) {
            is IOException -> ImportError.Storage
            else -> ImportError.Unknown(cause = e)
        }
    }

    // The core pipeline: Retrieve Asset -> Open Publication -> Extract Metadata -> Save to DB
    private suspend fun addBookFromFile(file: File): Try<Book, ImportError> {
        val url = storageManager.toUrl(file = file)

        return assetRetriever.retrieve(url = url)
            .mapFailure { ImportError.InvalidBook }
            .flatMap { asset ->
                // We open the publication here specifically to extract static metadata (title, author)
                // and the cover image. This relies on the Readium Streamer.
                publicationOpener.open(asset = asset, allowUserInteraction = false)
                    .mapFailure { ImportError.InvalidBook }
                    .flatMap { publication ->
                        try {
                            val coverPath = coverImageSaver.saveCover(publication = publication)
                            val book = mapToBook(
                                publication = publication,
                                asset = asset,
                                file = file,
                                coverPath = coverPath,
                            )

                            val id = booksDao.insertBook(book = book)
                            Try.success(success = book.copy(id = id))
                        } catch (e: Exception) {
                            Try.failure(failure = ImportError.Database(cause = e))
                        } finally {
                            // Vital: Close the publication to release file locks/memory.
                            // We only needed it momentarily for metadata extraction.
                            publication.close()
                        }
                    }
            }
    }

    private fun mapToBook(
        publication: Publication,
        asset: Asset,
        file: File,
        coverPath: String?,
    ): Book {
        return Book(
            href = file.absolutePath,
            title = publication.metadata.title ?: file.nameWithoutExtension,
            author = publication.metadata.authors.joinToString(separator = ", ") { it.name },
            identifier = publication.metadata.identifier ?: "",
            mediaType = asset.format.mediaType,
            cover = coverPath,
        )
    }
}
