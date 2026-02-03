package com.example.readiumandroidtestapp.core.data.book

import android.net.Uri
import android.os.Build
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
import org.readium.r2.shared.publication.Metadata
import org.readium.r2.shared.publication.Publication
import org.readium.r2.shared.util.AbsoluteUrl
import org.readium.r2.shared.util.Try
import org.readium.r2.shared.util.Url
import org.readium.r2.shared.util.asset.Asset
import org.readium.r2.shared.util.format.Format
import org.readium.r2.shared.util.mediatype.MediaType
import org.robolectric.annotation.Config
import java.io.ByteArrayInputStream
import java.io.File
import java.io.IOException

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.Q])
class DefaultBookImporterTest {

    private val storageGateway: StorageGateway = mockk()
    private val booksDao: BooksDao = mockk()
    private val assetRetriever: AssetRetrieverGateway = mockk()
    private val publicationOpener: PublicationOpenerGateway = mockk()
    private val httpGateway: HttpGateway = mockk()
    private val coverImageSaver: CoverImageSaver = mockk()
    private val dispatcher = StandardTestDispatcher()

    private lateinit var importer: DefaultBookImporter

    @Before
    fun setup() {
        importer = DefaultBookImporter(
            storageGateway,
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
        val metadata = mockk<Metadata>(relaxed = true)
        val format = mockk<Format>()

        coEvery { httpGateway.fetch(url) } returns Try.success(success = httpResult)
        every { storageGateway.resolveExtensionFromMimeType(mimeType = "application/epub+zip") } returns "epub"
        coEvery {
            storageGateway.saveFileFromStream(
                input = any(),
                extension = "epub",
            )
        } returns Try.success(success = savedFile)

        every { storageGateway.toUrl(file = savedFile) } returns fileUrl
        coEvery { assetRetriever.retrieve(url = fileUrl) } returns Result.success(value = asset)
        coEvery {
            publicationOpener.open(
                asset = asset,
                allowUserInteraction = false,
            )
        } returns Result.success(value = publication)

        every { publication.metadata } returns metadata
        every { metadata.title } returns "Test Book"
        every { metadata.authors } returns emptyList()
        every { metadata.identifier } returns "id"
        every { publication.close() } returns Unit

        every { asset.format } returns format
        every { format.mediaType } returns MediaType(string = "application/epub+zip")!!

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
    fun `importFromUri success`() = runTest(context = dispatcher) {
        val uri = mockk<Uri>()
        val inputStream = ByteArrayInputStream(ByteArray(0))
        val savedFile = File("saved.epub")
        val fileUrl = Url(url = "file:///saved.epub")!! as AbsoluteUrl
        val asset = mockk<Asset>()
        val publication = mockk<Publication>()
        val metadata = mockk<Metadata>(relaxed = true)
        val format = mockk<Format>()

        every { storageGateway.openInputStream(uri = uri) } returns inputStream
        every { storageGateway.resolveExtension(uri = uri) } returns "epub"
        coEvery {
            storageGateway.saveFileFromStream(
                input = any(),
                extension = "epub",
            )
        } returns Try.success(success = savedFile)

        every { storageGateway.toUrl(file = savedFile) } returns fileUrl
        coEvery { assetRetriever.retrieve(url = fileUrl) } returns Result.success(value = asset)
        coEvery {
            publicationOpener.open(
                asset = asset,
                allowUserInteraction = false,
            )
        } returns Result.success(value = publication)

        every { publication.metadata } returns metadata
        every { metadata.title } returns "URI Book"
        every { metadata.authors } returns emptyList()
        every { metadata.identifier } returns "id"
        every { publication.close() } returns Unit

        every { asset.format } returns format
        every { format.mediaType } returns MediaType(string = "application/epub+zip")!!

        coEvery { coverImageSaver.saveCover(publication = publication) } returns null
        coEvery { booksDao.insertBook(book = any()) } returns 2L

        val result = importer.importFromUri(uri = uri)

        assertTrue(result is Try.Success)
        assertEquals("URI Book", (result as Try.Success).value.title)
    }

    @Test
    fun `importFromUri fails on storage error`() = runTest(context = dispatcher) {
        val uri = mockk<Uri>()
        every { storageGateway.openInputStream(uri) } throws IOException("Disk error")

        val result = importer.importFromUri(uri)

        assertTrue(result is Try.Failure)
        assertTrue((result as Try.Failure).value is ImportError.Storage)
    }
}
