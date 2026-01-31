package com.example.readiumandroidtestapp.core.data.book

import android.net.Uri
import com.example.readiumandroidtestapp.core.data.database.BooksDao
import com.example.readiumandroidtestapp.core.domain.gateway.AssetRetrieverGateway
import com.example.readiumandroidtestapp.core.domain.gateway.PublicationOpenerGateway
import com.example.readiumandroidtestapp.core.domain.network.HttpGateway
import com.example.readiumandroidtestapp.core.domain.network.HttpResult
import com.example.readiumandroidtestapp.core.domain.storage.StorageGateway
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.readium.r2.shared.publication.Publication
import org.readium.r2.shared.util.AbsoluteUrl
import org.readium.r2.shared.util.Try
import org.readium.r2.shared.util.asset.Asset
import org.readium.r2.shared.util.format.Format
import org.readium.r2.shared.util.mediatype.MediaType
import org.robolectric.RobolectricTestRunner
import java.io.File
import java.io.InputStream

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class DefaultBookImporterTest {

    private val storageGateway: StorageGateway = mockk(relaxed = true)
    private val booksDao: BooksDao = mockk(relaxed = true)
    private val assetRetriever: AssetRetrieverGateway = mockk()
    private val publicationOpener: PublicationOpenerGateway = mockk()
    private val httpGateway: HttpGateway = mockk()
    private val coverImageSaver: CoverImageSaver = mockk()
    private val testDispatcher = UnconfinedTestDispatcher()

    private val importer = DefaultBookImporter(
        storageGateway = storageGateway,
        booksDao = booksDao,
        assetRetriever = assetRetriever,
        publicationOpener = publicationOpener,
        httpGateway = httpGateway,
        coverImageSaver = coverImageSaver,
        ioDispatcher = testDispatcher,
    )

    @Test
    fun `importFromUrl success`() = runTest {
        val url = AbsoluteUrl(url = "http://example.com/book.epub")!!
        val file = File("book.epub")
        val localUrl = AbsoluteUrl(url = "file:///book.epub")!!
        val asset = mockk<Asset>()
        val format = mockk<Format>()
        val publication = mockk<Publication>(relaxed = true)
        val coverPath = "cover.jpg"
        val bookId = 123L

        // HttpGateway
        coEvery { httpGateway.fetch(url = url) } returns Try.success(
            success = HttpResult(
                body = ByteArray(0),
                contentType = "application/epub+zip",
            ),
        )

        // StorageGateway
        every { storageGateway.resolveExtensionFromMimeType(mimeType = "application/epub+zip") } returns "epub"
        coEvery {
            storageGateway.saveFileFromStream(
                input = any(),
                extension = "epub",
            )
        } returns Try.success(success = file)
        every { storageGateway.toUrl(file = file) } returns localUrl

        // AssetRetriever
        coEvery { assetRetriever.retrieve(url = localUrl) } returns Result.success(value = asset)
        every { asset.format } returns format
        every { format.mediaType } returns MediaType(string = "application/epub+zip")!!

        // PublicationOpener
        coEvery {
            publicationOpener.open(
                asset = asset,
                allowUserInteraction = false,
            )
        } returns Result.success(value = publication)

        // CoverImageSaver
        coEvery { coverImageSaver.saveCover(publication = publication) } returns coverPath

        // BooksDao
        coEvery { booksDao.insertBook(book = any()) } returns bookId

        // Run
        val result = importer.importFromUrl(url = url)

        assertTrue(result.isSuccess)
        val book = result.getOrNull()
        assertEquals(bookId, book?.id)

        // Verification
        coVerify { httpGateway.fetch(url = url) }
        coVerify { storageGateway.saveFileFromStream(input = any(), extension = "epub") }
        coVerify { publicationOpener.open(asset = asset, allowUserInteraction = false) }
        coVerify { booksDao.insertBook(book = any()) }
        verify { publication.close() }
    }

    @Test
    fun `importFromUri success`() = runTest {
        val uri = mockk<Uri>()
        val file = File("book.epub")
        val localUrl = AbsoluteUrl("file:///book.epub")!!
        val asset = mockk<Asset>()
        val format = mockk<Format>()
        val publication = mockk<Publication>(relaxed = true)
        val coverPath = "cover.jpg"
        val bookId = 123L
        val inputStream = mockk<InputStream>(relaxed = true)

        // StorageGateway
        every { storageGateway.openInputStream(uri = uri) } returns inputStream
        every { storageGateway.resolveExtension(uri = uri) } returns "epub"
        coEvery {
            storageGateway.saveFileFromStream(
                input = inputStream,
                extension = "epub",
            )
        } returns Try.success(success = file)
        every { storageGateway.toUrl(file = file) } returns localUrl

        // AssetRetriever
        coEvery { assetRetriever.retrieve(url = localUrl) } returns Result.success(value = asset)
        every { asset.format } returns format
        every { format.mediaType } returns MediaType("application/epub+zip")!!

        // PublicationOpener
        coEvery {
            publicationOpener.open(
                asset = asset,
                allowUserInteraction = false,
            )
        } returns Result.success(value = publication)

        // CoverImageSaver
        coEvery { coverImageSaver.saveCover(publication = publication) } returns coverPath

        // BooksDao
        coEvery { booksDao.insertBook(book = any()) } returns bookId

        // Run
        val result = importer.importFromUri(uri = uri)

        assertTrue(result.isSuccess)
        val book = result.getOrNull()
        assertEquals(bookId, book?.id)

        // Verification
        coVerify { storageGateway.openInputStream(uri = uri) }
        coVerify { storageGateway.saveFileFromStream(input = inputStream, extension = "epub") }
        coVerify { publicationOpener.open(asset = asset, allowUserInteraction = false) }
        coVerify { booksDao.insertBook(book = any()) }
        verify { publication.close() }
    }
}
