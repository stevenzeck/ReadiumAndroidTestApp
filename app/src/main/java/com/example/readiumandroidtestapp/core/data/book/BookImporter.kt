package com.example.readiumandroidtestapp.core.data.book

import android.graphics.Bitmap
import android.net.Uri
import android.webkit.MimeTypeMap
import com.example.readiumandroidtestapp.core.data.database.BooksDao
import com.example.readiumandroidtestapp.core.domain.model.Book
import com.example.readiumandroidtestapp.core.domain.storage.StorageGateway
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.readium.r2.shared.publication.Publication
import org.readium.r2.shared.publication.services.cover
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
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.UUID
import javax.inject.Inject

/**
 * Orchestrates the import of books into the application.
 *
 * This class handles the entire pipeline of:
 * 1. Fetching the content (from network or local URI).
 * 2. Saving the content to the app's internal storage (filesDir).
 * 3. Opening the saved file with Readium to parse metadata and extract the cover.
 * 4. Persisting the book metadata to the database.
 */
class BookImporter @Inject constructor(
    private val storageGateway: StorageGateway,
    private val booksDao: BooksDao,
    private val assetRetriever: AssetRetriever,
    private val publicationOpener: PublicationOpener,
    private val httpClient: HttpClient,
) {

    /**
     * Imports a book from a remote URL.
     *
     * We download the file to disk first because Readium parsers work best with seekable local files,
     * and we need to persist the book for offline access anyway.
     */
    suspend fun importFromUrl(url: AbsoluteUrl): Try<Book, ImportError> =
        withContext(context = Dispatchers.IO) {
            val request = HttpRequest(url = url)

            httpClient.fetch(request = request)
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
                                MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
                        }
                    }

                    // Save to local storage first to ensure we have a file handle for the parser.
                    saveFileToDisk(extension = extension, input = stream)
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
        withContext(context = Dispatchers.IO) {
            try {
                val inputStream = storageGateway.openInputStream(uri = uri)
                    ?: throw IOException("Could not open input stream")
                inputStream.use { stream ->
                    saveFileToDisk(
                        extension = storageGateway.resolveExtension(uri = uri),
                        input = stream,
                    )
                }
            } catch (e: Exception) {
                mapIOError(e = e)
            }
                .flatMap { file ->
                    addBookFromFile(file = file)
                }
                .onFailure { error ->
                    Timber.e(message = "Error adding book from URI: $error")
                }
        }

    // Assigns a unique filename (UUID) to avoid collisions and writes the input stream to disk.
    private fun saveFileToDisk(
        extension: String?,
        input: InputStream,
    ): Try<File, ImportError> {
        return try {
            val safeExtension = when {
                extension == null -> ".epub"
                extension.startsWith(prefix = ".") -> extension
                else -> ".$extension"
            }
            val filename = "${UUID.randomUUID()}$safeExtension"
            val file = File(storageGateway.filesDir, filename)

            FileOutputStream(file).use { output ->
                input.copyTo(out = output)
            }
            Try.success(success = file)
        } catch (e: Exception) {
            mapIOError(e = e)
        }
    }

    private fun <T> mapIOError(e: Exception): Try<T, ImportError> {
        Timber.e(t = e, message = "Storage error during import")
        return when (e) {
            is IOException -> Try.failure(failure = ImportError.Storage)
            else -> Try.failure(failure = ImportError.Unknown(cause = e))
        }
    }

    // The core pipeline: Retrieve Asset -> Open Publication -> Extract Metadata -> Save to DB
    private suspend fun addBookFromFile(file: File): Try<Book, ImportError> {
        val url = storageGateway.toUrl(file)
            ?: return Try.failure(failure = ImportError.Unknown(cause = Exception("Could not convert file to URL")))

        return assetRetriever.retrieve(url = url)
            .mapFailure { ImportError.InvalidBook }
            .flatMap { asset ->
                // We open the publication here specifically to extract static metadata (title, author)
                // and the cover image. This relies on the Readium Streamer.
                publicationOpener.open(asset = asset, allowUserInteraction = false)
                    .mapFailure { ImportError.InvalidBook }
                    .flatMap { publication ->
                        try {
                            val coverPath = saveCoverToDisk(publication = publication)
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

    private suspend fun saveCoverToDisk(publication: Publication): String? {
        return try {
            val coverBitmap = publication.cover() ?: return null
            val coverFile = File(storageGateway.filesDir, "covers/${UUID.randomUUID()}.jpg")
            coverFile.parentFile?.mkdirs()

            FileOutputStream(coverFile).use { out ->
                coverBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
            coverFile.absolutePath
        } catch (e: Exception) {
            Timber.w(t = e, message = "Failed to save cover image")
            null
        }
    }
}

sealed class ImportError {
    data object Network : ImportError()
    data object Storage : ImportError()
    data object InvalidBook : ImportError()
    data class Database(val cause: Exception) : ImportError()
    data class Unknown(val cause: Exception) : ImportError()
}
