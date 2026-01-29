package com.example.readiumandroidtestapp.core.data.book

import android.net.Uri
import com.example.readiumandroidtestapp.core.data.database.BooksDao
import com.example.readiumandroidtestapp.core.domain.network.HttpGateway
import com.example.readiumandroidtestapp.core.domain.network.HttpResult
import com.example.readiumandroidtestapp.core.domain.storage.StorageGateway
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test
import org.readium.r2.shared.publication.Publication
import org.readium.r2.shared.util.AbsoluteUrl
import org.readium.r2.shared.util.Try
import org.readium.r2.shared.util.asset.Asset
import org.readium.r2.shared.util.asset.AssetRetriever
import org.readium.r2.shared.util.format.FormatHints
import org.readium.r2.streamer.PublicationOpener
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream
import java.nio.file.Files

@OptIn(ExperimentalCoroutinesApi::class)
class BookImporterTest {

    private val tempDir = Files.createTempDirectory("test_storage").toFile()
    private val mockUrl = mockk<AbsoluteUrl>()

    private val fakeGateway = object : StorageGateway {
        override val filesDir = tempDir

        override fun openInputStream(uri: Uri): InputStream {
            return ByteArrayInputStream("Fake Book Content".toByteArray())
        }

        override fun resolveExtension(uri: Uri) = "epub"

        override fun resolveExtensionFromMimeType(mimeType: String): String? {
            return if (mimeType == "application/epub+zip") "epub" else null
        }

        override fun toUrl(file: File): AbsoluteUrl {
            return mockUrl
        }

        override fun deleteFile(path: String): Boolean {
            return File(path).delete()
        }

        override fun saveFileFromStream(
            input: InputStream,
            extension: String?,
        ): Try<File, Exception> {
            val name = "test_${System.nanoTime()}.${extension ?: "epub"}"
            val file = File(filesDir, name)
            java.io.FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
            return Try.success(success = file)
        }
    }

    private val booksDao: BooksDao = mockk(relaxed = true)
    private val assetRetriever: AssetRetriever = mockk()
    private val publicationOpener: PublicationOpener = mockk()
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
                url = mockUrl,
                formatHints = any<FormatHints>(),
            )
        } returns Try.success(success = mockAsset)

        coEvery {
            publicationOpener.open(
                asset = any<Asset>(),
                allowUserInteraction = any<Boolean>(),
            )
        } returns Try.success(success = mockPublication)

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
        val remoteUrl = mockk<AbsoluteUrl>()
        every { remoteUrl.extension } returns null

        val httpResult = HttpResult(
            body = "Fake Remote Content".toByteArray(),
            contentType = "application/epub+zip",
        )

        coEvery { httpGateway.fetch(url = any()) } returns Try.success(success = httpResult)

        val mockAsset = mockk<Asset>(relaxed = true)
        val mockPublication = mockk<Publication>(relaxed = true)

        coEvery {
            assetRetriever.retrieve(
                url = mockUrl,
                formatHints = any<FormatHints>(),
            )
        } returns Try.success(success = mockAsset)

        coEvery {
            publicationOpener.open(
                asset = any<Asset>(),
                allowUserInteraction = any<Boolean>(),
            )
        } returns Try.success(success = mockPublication)

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
                url = mockUrl,
                formatHints = any<FormatHints>(),
            )
        } returns Try.success(success = mockAsset)

        coEvery {
            publicationOpener.open(
                asset = any<Asset>(),
                allowUserInteraction = any<Boolean>(),
            )
        } returns Try.success(success = mockPublication)

        coEvery { coverImageSaver.saveCover(publication = mockPublication) } returns coverPath
        coEvery { booksDao.insertBook(book = any()) } returns 1L

        val result = importer.importFromUri(uri = mockk(relaxed = true))

        Assert.assertTrue(result.isSuccess)
        val book = result.getOrNull()
        Assert.assertEquals(coverPath, book?.cover)

        coVerify { coverImageSaver.saveCover(publication = mockPublication) }
    }
}
