package com.example.readiumandroidtestapp.core.data.book

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.readiumandroidtestapp.core.data.database.BooksDao
import com.example.readiumandroidtestapp.core.data.storage.FakeStorageGateway
import com.example.readiumandroidtestapp.core.domain.gateway.AssetRetrieverGateway
import com.example.readiumandroidtestapp.core.domain.gateway.PublicationOpenerGateway
import com.example.readiumandroidtestapp.core.domain.model.Book
import com.example.readiumandroidtestapp.core.domain.network.HttpGateway
import com.example.readiumandroidtestapp.core.domain.network.HttpResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
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
import org.readium.r2.shared.util.mediatype.MediaType
import java.io.File
import java.nio.file.Files

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class BookImporterTest {

    private val tempDir = Files.createTempDirectory("test_storage").toFile()
    private val fakeStorageGateway = FakeStorageGateway(filesDir = tempDir)

    private val booksDao: BooksDao = mockk(relaxed = true)
    private val assetRetriever: AssetRetrieverGateway = mockk()
    private val publicationOpener: PublicationOpenerGateway = mockk()
    private val httpGateway: HttpGateway = mockk()
    private val coverImageSaver: CoverImageSaver = mockk()

    private val importer = BookImporter(
        storageGateway = fakeStorageGateway,
        booksDao = booksDao,
        assetRetriever = assetRetriever,
        publicationOpener = publicationOpener,
        httpGateway = httpGateway,
        coverImageSaver = coverImageSaver,
        ioDispatcher = UnconfinedTestDispatcher(),
    )

    @Test
    fun `importFromUri saves file and inserts book into database`() = runTest {
        // Arrange
        val mockAsset = mockk<Asset>()
        every { mockAsset.format.mediaType } returns MediaType.EPUB

        val mockPublication = mockk<Publication>(relaxed = true)
        every { mockPublication.metadata.title } returns "My Local Book"

        coEvery {
            assetRetriever.retrieve(url = any())
        } returns Result.success(value = mockAsset)

        coEvery {
            publicationOpener.open(asset = mockAsset, allowUserInteraction = false)
        } returns Result.success(value = mockPublication)

        coEvery { coverImageSaver.saveCover(publication = mockPublication) } returns null
        coEvery { booksDao.insertBook(book = any()) } returns 1L

        // Act
        val result = importer.importFromUri(uri = mockk(relaxed = true))

        // Assert
        assertTrue("Result should be success", result.isSuccess)

        val savedBook = result.getOrNull()!!
        val savedFile = File(savedBook.href)
        assertEquals(
            "File content should match FakeStorageGateway default",
            "Fake Book Content",
            savedFile.readText(),
        )

        val bookSlot = slot<Book>()
        coVerify { booksDao.insertBook(book = capture(bookSlot)) }

        assertEquals("My Local Book", bookSlot.captured.title)
        assertEquals(MediaType.EPUB, bookSlot.captured.mediaType)
    }

    @Test
    fun `importFromUrl downloads file and inserts book`() = runTest {
        // Arrange
        val remoteContent = "Fake Remote Content"
        val httpResult = HttpResult(
            body = remoteContent.toByteArray(),
            contentType = "application/epub+zip",
        )
        coEvery { httpGateway.fetch(url = any()) } returns Try.success(success = httpResult)

        val mockAsset = mockk<Asset>()
        every { mockAsset.format.mediaType } returns MediaType.EPUB

        val mockPublication = mockk<Publication>(relaxed = true)
        every { mockPublication.metadata.title } returns "Remote Book"

        coEvery {
            assetRetriever.retrieve(url = any())
        } returns Result.success(value = mockAsset)

        coEvery {
            publicationOpener.open(asset = mockAsset, allowUserInteraction = false)
        } returns Result.success(value = mockPublication)

        coEvery { coverImageSaver.saveCover(publication = mockPublication) } returns null
        coEvery { booksDao.insertBook(book = any()) } returns 1L

        // Act
        val remoteUrl = AbsoluteUrl(url = "http://example.com/book.epub")!!
        val result = importer.importFromUrl(url = remoteUrl)

        // Assert
        assertTrue(result.isSuccess)

        val savedBook = result.getOrNull()!!
        val savedFile = File(savedBook.href)
        assertEquals(remoteContent, savedFile.readText())

        val bookSlot = slot<Book>()
        coVerify { booksDao.insertBook(book = capture(bookSlot)) }
        assertEquals("Remote Book", bookSlot.captured.title)
    }
}
