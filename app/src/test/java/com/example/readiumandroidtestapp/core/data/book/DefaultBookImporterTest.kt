package com.example.readiumandroidtestapp.core.data.book

import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.readiumandroidtestapp.core.data.database.BooksDao
import com.example.readiumandroidtestapp.core.domain.gateway.AssetRetrieverGateway
import com.example.readiumandroidtestapp.core.domain.gateway.PublicationOpenerGateway
import com.example.readiumandroidtestapp.core.domain.network.HttpGateway
import com.example.readiumandroidtestapp.core.domain.network.HttpResult
import com.example.readiumandroidtestapp.core.domain.storage.StorageGateway
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.readium.r2.shared.publication.LocalizedString
import org.readium.r2.shared.publication.Metadata
import org.readium.r2.shared.publication.Publication
import org.readium.r2.shared.util.AbsoluteUrl
import org.readium.r2.shared.util.FileExtension
import org.readium.r2.shared.util.Try
import org.readium.r2.shared.util.Url
import org.readium.r2.shared.util.asset.Asset
import org.readium.r2.shared.util.format.Format
import org.readium.r2.shared.util.format.FormatSpecification
import org.readium.r2.shared.util.format.Specification
import org.readium.r2.shared.util.mediatype.MediaType
import java.io.ByteArrayInputStream
import java.io.File
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class DefaultBookImporterTest {

    private val fakeStorageGateway: StorageGateway = mockk()
    private val booksDao: BooksDao = mockk()
    private val assetRetriever: AssetRetrieverGateway = mockk()
    private val publicationOpener: PublicationOpenerGateway = mockk()
    private val httpGateway: HttpGateway = mockk()
    private val coverImageSaver: CoverImageSaver = mockk()
    private val dispatcher = StandardTestDispatcher()

    private lateinit var importer: BookImporter

    @Before
    fun setup() {
        importer = BookImporter(
            fakeStorageGateway,
            booksDao,
            assetRetriever,
            publicationOpener,
            httpGateway,
            coverImageSaver,
            dispatcher,
        )
    }

    @Test
    fun `importFromUrl success`() = runTest(dispatcher) {
        val url = Url(url = "http://example.com/book.epub")!! as AbsoluteUrl
        val httpResult = HttpResult(body = ByteArray(0), contentType = "application/epub+zip")
        val savedFile = File("saved.epub")
        val fileUrl = Url(url = "file:///saved.epub")!! as AbsoluteUrl
        val asset = mockk<Asset>()
        val publication = mockk<Publication>()
        val metadata = Metadata(
            localizedTitle = LocalizedString(value = "Test Book"),
            identifier = "id",
        )
        val format = Format(
            specification = FormatSpecification(Specification.Epub),
            mediaType = MediaType(string = "application/epub+zip")!!,
            fileExtension = FileExtension(value = "epub"),
        )

        coEvery { httpGateway.fetch(url) } returns Try.success(success = httpResult)
        every { fakeStorageGateway.resolveExtensionFromMimeType(mimeType = "application/epub+zip") } returns "epub"
        coEvery {
            fakeStorageGateway.saveFileFromStream(
                input = any(),
                extension = "epub",
            )
        } returns Try.success(success = savedFile)

        every { fakeStorageGateway.toUrl(file = savedFile) } returns fileUrl
        coEvery { assetRetriever.retrieve(url = fileUrl) } returns Result.success(value = asset)
        coEvery {
            publicationOpener.open(
                asset = asset,
                allowUserInteraction = false,
            )
        } returns Result.success(value = publication)

        every { publication.metadata } returns metadata
        every { publication.close() } returns Unit
        every { asset.format } returns format

        coEvery { coverImageSaver.saveCover(publication) } returns "cover.jpg"
        coEvery { booksDao.insertBook(book = any()) } returns 1L

        val result = importer.importFromUrl(url)

        assertTrue(result is Try.Success)
        val book = (result as Try.Success).value
        assertEquals("Test Book", book.title)
        assertEquals(1L, book.id)

        verify { publication.close() }
    }

    @Test
    fun `importFromUrl fails on network error`() = runTest(context = dispatcher) {
        val url = Url(url = "http://example.com/book.epub")!! as AbsoluteUrl
        coEvery { httpGateway.fetch(url) } returns Try.failure(failure = Exception("Network error"))

        val result = importer.importFromUrl(url = url)

        assertTrue(result is Try.Failure)
        assertTrue((result as Try.Failure).value is ImportError.Network)
    }

    @Test
    fun `importFromUrl fails on storage error`() = runTest(context = dispatcher) {
        val url = Url(url = "http://example.com/book.epub")!! as AbsoluteUrl
        val httpResult = HttpResult(body = ByteArray(0), contentType = "application/epub+zip")
        val ioException = IOException("Storage full")

        coEvery { httpGateway.fetch(url) } returns Try.success(success = httpResult)
        every { fakeStorageGateway.resolveExtensionFromMimeType(mimeType = "application/epub+zip") } returns "epub"
        coEvery {
            fakeStorageGateway.saveFileFromStream(
                input = any(),
                extension = "epub",
            )
        } returns Try.failure(failure = ioException)

        val result = importer.importFromUrl(url = url)

        assertTrue(result is Try.Failure)
        assertTrue((result as Try.Failure).value is ImportError.Storage)
    }

    @Test
    fun `importFromUri success`() = runTest(context = dispatcher) {
        val uri = Uri.parse("content://com.example/book")
        val inputStream = ByteArrayInputStream(ByteArray(0))
        val savedFile = File("saved.epub")
        val fileUrl = Url(url = "file:///saved.epub")!! as AbsoluteUrl
        val asset = mockk<Asset>()
        val publication = mockk<Publication>()
        val metadata = Metadata(
            localizedTitle = LocalizedString(value = "URI Book"),
            identifier = "id",
        )
        val format = Format(
            specification = FormatSpecification(Specification.Epub),
            mediaType = MediaType(string = "application/epub+zip")!!,
            fileExtension = FileExtension(value = "epub"),
        )

        every { fakeStorageGateway.openInputStream(uri = uri) } returns inputStream
        every { fakeStorageGateway.resolveExtension(uri = uri) } returns "epub"
        coEvery {
            fakeStorageGateway.saveFileFromStream(
                input = any(),
                extension = "epub",
            )
        } returns Try.success(success = savedFile)

        every { fakeStorageGateway.toUrl(file = savedFile) } returns fileUrl
        coEvery { assetRetriever.retrieve(url = fileUrl) } returns Result.success(value = asset)
        coEvery {
            publicationOpener.open(
                asset = asset,
                allowUserInteraction = false,
            )
        } returns Result.success(value = publication)

        every { publication.metadata } returns metadata
        every { publication.close() } returns Unit
        every { asset.format } returns format

        coEvery { coverImageSaver.saveCover(publication = publication) } returns null
        coEvery { booksDao.insertBook(book = any()) } returns 2L

        val result = importer.importFromUri(uri = uri)

        assertTrue(result is Try.Success)
        assertEquals("URI Book", (result as Try.Success).value.title)
    }

    @Test
    fun `importFromUri fails on storage error`() = runTest(context = dispatcher) {
        val uri = Uri.parse("content://com.example/book")
        every { fakeStorageGateway.openInputStream(uri) } throws IOException("Disk error")

        val result = importer.importFromUri(uri)

        assertTrue(result is Try.Failure)
        assertTrue((result as Try.Failure).value is ImportError.Storage)
    }

    @Test
    fun `addBookFromFile fails on invalid book (retriever error)`() =
        runTest(context = dispatcher) {
            val uri = Uri.parse("content://com.example/book")
            val inputStream = ByteArrayInputStream(ByteArray(0))
            val savedFile = File("saved.epub")
            val fileUrl = Url(url = "file:///saved.epub")!! as AbsoluteUrl

            every { fakeStorageGateway.openInputStream(uri = uri) } returns inputStream
            every { fakeStorageGateway.resolveExtension(uri = uri) } returns "epub"
            coEvery {
                fakeStorageGateway.saveFileFromStream(
                    input = any(),
                    extension = "epub",
                )
            } returns Try.success(success = savedFile)

            every { fakeStorageGateway.toUrl(file = savedFile) } returns fileUrl
            coEvery { assetRetriever.retrieve(url = fileUrl) } returns Result.failure(
                exception = Exception("Retrieve failed"),
            )

            val result = importer.importFromUri(uri = uri)

            assertTrue(result is Try.Failure)
            assertTrue((result as Try.Failure).value is ImportError.InvalidBook)
        }

    @Test
    fun `addBookFromFile fails on invalid book (opener error)`() = runTest(context = dispatcher) {
        val uri = Uri.parse("content://com.example/book")
        val inputStream = ByteArrayInputStream(ByteArray(0))
        val savedFile = File("saved.epub")
        val fileUrl = Url(url = "file:///saved.epub")!! as AbsoluteUrl
        val asset = mockk<Asset>()

        every { fakeStorageGateway.openInputStream(uri = uri) } returns inputStream
        every { fakeStorageGateway.resolveExtension(uri = uri) } returns "epub"
        coEvery {
            fakeStorageGateway.saveFileFromStream(
                input = any(),
                extension = "epub",
            )
        } returns Try.success(success = savedFile)

        every { fakeStorageGateway.toUrl(file = savedFile) } returns fileUrl
        coEvery { assetRetriever.retrieve(url = fileUrl) } returns Result.success(value = asset)
        coEvery {
            publicationOpener.open(
                asset = asset,
                allowUserInteraction = false,
            )
        } returns Result.failure(exception = Exception("Open failed"))

        val result = importer.importFromUri(uri = uri)

        assertTrue(result is Try.Failure)
        assertTrue((result as Try.Failure).value is ImportError.InvalidBook)
    }

    @Test
    fun `addBookFromFile fails on database error`() = runTest(context = dispatcher) {
        val uri = Uri.parse("content://com.example/book")
        val inputStream = ByteArrayInputStream(ByteArray(0))
        val savedFile = File("saved.epub")
        val fileUrl = Url(url = "file:///saved.epub")!! as AbsoluteUrl
        val asset = mockk<Asset>()
        val publication = mockk<Publication>()

        val metadata = Metadata(
            localizedTitle = LocalizedString("Test Book"),
            identifier = "id",
        )
        val format = Format(
            specification = FormatSpecification(Specification.Epub),
            mediaType = MediaType(string = "application/epub+zip")!!,
            fileExtension = FileExtension(value = "epub"),
        )

        every { fakeStorageGateway.openInputStream(uri = uri) } returns inputStream
        every { fakeStorageGateway.resolveExtension(uri = uri) } returns "epub"
        coEvery {
            fakeStorageGateway.saveFileFromStream(
                input = any(),
                extension = "epub",
            )
        } returns Try.success(success = savedFile)

        every { fakeStorageGateway.toUrl(file = savedFile) } returns fileUrl
        coEvery { assetRetriever.retrieve(url = fileUrl) } returns Result.success(value = asset)
        coEvery {
            publicationOpener.open(
                asset = asset,
                allowUserInteraction = false,
            )
        } returns Result.success(value = publication)

        every { publication.metadata } returns metadata
        every { publication.close() } returns Unit
        every { asset.format } returns format

        coEvery { coverImageSaver.saveCover(publication = publication) } returns null
        val dbException = RuntimeException("DB error")
        coEvery { booksDao.insertBook(book = any()) } throws dbException

        val result = importer.importFromUri(uri = uri)

        assertTrue(result is Try.Failure)
        assertTrue((result as Try.Failure).value is ImportError.Database)
        verify { publication.close() }
    }

    @Test
    fun `mapToBook handles missing metadata`() = runTest(context = dispatcher) {
        val uri = Uri.parse("content://com.example/fallback_title.epub")
        val inputStream = ByteArrayInputStream(ByteArray(0))
        val savedFile = File("fallback_title.epub")
        val fileUrl = Url(url = "file:///fallback_title.epub")!! as AbsoluteUrl
        val asset = mockk<Asset>()
        val publication = mockk<Publication>()
        val metadata = Metadata(
            localizedTitle = null,
            identifier = null,
        )
        val format = Format(
            specification = FormatSpecification(Specification.Epub),
            mediaType = MediaType(string = "application/epub+zip")!!,
            fileExtension = FileExtension(value = "epub"),
        )

        every { fakeStorageGateway.openInputStream(uri = uri) } returns inputStream
        every { fakeStorageGateway.resolveExtension(uri = uri) } returns "epub"
        coEvery {
            fakeStorageGateway.saveFileFromStream(
                input = any(),
                extension = "epub",
            )
        } returns Try.success(success = savedFile)

        every { fakeStorageGateway.toUrl(file = savedFile) } returns fileUrl
        coEvery { assetRetriever.retrieve(url = fileUrl) } returns Result.success(value = asset)
        coEvery {
            publicationOpener.open(
                asset = asset,
                allowUserInteraction = false,
            )
        } returns Result.success(value = publication)

        every { publication.metadata } returns metadata
        every { publication.close() } returns Unit
        every { asset.format } returns format

        coEvery { coverImageSaver.saveCover(publication = publication) } returns null
        coEvery { booksDao.insertBook(book = any()) } returns 3L

        val result = importer.importFromUri(uri = uri)

        assertTrue(result is Try.Success)
        val book = (result as Try.Success).value

        assertEquals("fallback_title", book.title)
        assertEquals("", book.author)
        assertEquals("", book.identifier)
    }
}
