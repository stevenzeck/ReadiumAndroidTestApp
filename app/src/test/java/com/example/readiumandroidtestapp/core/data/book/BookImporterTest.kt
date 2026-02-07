package com.example.readiumandroidtestapp.core.data.book

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.readiumandroidtestapp.core.data.database.BooksDao
import com.example.readiumandroidtestapp.core.data.storage.FakeStorageGateway
import com.example.readiumandroidtestapp.core.domain.gateway.AssetRetrieverGateway
import com.example.readiumandroidtestapp.core.domain.gateway.PublicationOpenerGateway
import com.example.readiumandroidtestapp.core.domain.network.HttpGateway
import com.example.readiumandroidtestapp.core.domain.network.HttpResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.readium.r2.shared.publication.Publication
import org.readium.r2.shared.util.AbsoluteUrl
import org.readium.r2.shared.util.Try
import org.readium.r2.shared.util.asset.Asset
import java.io.File
import java.nio.file.Files

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class BookImporterTest {

    private val tempDir = Files.createTempDirectory("test_storage").toFile()

    private val fakeGateway = FakeStorageGateway(filesDir = tempDir)

    private val booksDao: BooksDao = mockk(relaxed = true)
    private val assetRetriever: AssetRetrieverGateway = mockk()
    private val publicationOpener: PublicationOpenerGateway = mockk()
    private val httpGateway: HttpGateway = mockk()
    private val coverImageSaver: CoverImageSaver = mockk()
    private val testDispatcher = UnconfinedTestDispatcher()

    private val importer = DefaultBookImporter(
        storageGateway = fakeGateway,
        booksDao = booksDao,
        assetRetriever = assetRetriever,
        publicationOpener = publicationOpener,
        httpGateway = httpGateway,
        coverImageSaver = coverImageSaver,
        ioDispatcher = testDispatcher,
    )

    @Test
    fun `importFromUri saves file to storage directory`() = runTest {
        val mockAsset = mockk<Asset>(relaxed = true)
        val mockPublication = mockk<Publication>(relaxed = true)

        coEvery {
            assetRetriever.retrieve(
                url = any(),
            )
        } returns Result.success(value = mockAsset)

        coEvery {
            publicationOpener.open(
                asset = any<Asset>(),
                allowUserInteraction = any<Boolean>(),
            )
        } returns Result.success(value = mockPublication)

        coEvery { coverImageSaver.saveCover(publication = mockPublication) } returns null
        coEvery { booksDao.insertBook(book = any()) } returns 1L

        val result = importer.importFromUri(uri = mockk(relaxed = true))

        Assert.assertTrue("Operation failed: ${result.failureOrNull()}", result.isSuccess)

        val savedBook = result.getOrNull()
        if (savedBook != null) {
            val savedFile = File(savedBook.href)
            Assert.assertTrue("File should exist in temp dir", savedFile.exists())
            Assert.assertEquals("Fake Book Content", savedFile.readText())
        }
    }

    @Test
    fun `importFromUrl downloads and saves file`() = runTest {
        val remoteUrl = AbsoluteUrl(url = "http://example.com/book")!!

        val httpResult = HttpResult(
            body = "Fake Remote Content".toByteArray(),
            contentType = "application/epub+zip",
        )

        coEvery { httpGateway.fetch(url = any()) } returns Try.success(success = httpResult)

        val mockAsset = mockk<Asset>(relaxed = true)
        val mockPublication = mockk<Publication>(relaxed = true)

        coEvery {
            assetRetriever.retrieve(
                url = any(),
            )
        } returns Result.success(value = mockAsset)

        coEvery {
            publicationOpener.open(
                asset = any<Asset>(),
                allowUserInteraction = any<Boolean>(),
            )
        } returns Result.success(value = mockPublication)

        coEvery { coverImageSaver.saveCover(publication = mockPublication) } returns null
        coEvery { booksDao.insertBook(book = any()) } returns 1L

        val result = importer.importFromUrl(url = remoteUrl)

        Assert.assertTrue("Operation failed: ${result.failureOrNull()}", result.isSuccess)

        val savedBook = result.getOrNull()
        if (savedBook != null) {
            val savedFile = File(savedBook.href)
            Assert.assertTrue("File should exist", savedFile.exists())
            Assert.assertTrue("File should have .epub extension", savedFile.name.endsWith(".epub"))
            Assert.assertEquals("Fake Remote Content", savedFile.readText())
        }
    }

    @Test
    fun `book cover is saved when available`() = runTest {
        val mockAsset = mockk<Asset>(relaxed = true)
        val mockPublication = mockk<Publication>(relaxed = true)
        val coverPath = "/path/to/cover.jpg"

        coEvery {
            assetRetriever.retrieve(
                url = any(),
            )
        } returns Result.success(value = mockAsset)

        coEvery {
            publicationOpener.open(
                asset = any<Asset>(),
                allowUserInteraction = any<Boolean>(),
            )
        } returns Result.success(value = mockPublication)

        coEvery { coverImageSaver.saveCover(publication = mockPublication) } returns coverPath
        coEvery { booksDao.insertBook(book = any()) } returns 1L

        val result = importer.importFromUri(uri = mockk(relaxed = true))

        Assert.assertTrue(result.isSuccess)
        val book = result.getOrNull()
        Assert.assertEquals(coverPath, book?.cover)

        coVerify { coverImageSaver.saveCover(publication = mockPublication) }
    }
}
