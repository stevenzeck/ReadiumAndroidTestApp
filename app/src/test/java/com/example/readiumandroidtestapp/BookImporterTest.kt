package com.example.readiumandroidtestapp

import android.net.Uri
import com.example.readiumandroidtestapp.core.data.book.BookImporter
import com.example.readiumandroidtestapp.core.data.database.BooksDao
import com.example.readiumandroidtestapp.core.domain.storage.StorageGateway
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.readium.r2.shared.publication.Publication
import org.readium.r2.shared.util.AbsoluteUrl
import org.readium.r2.shared.util.Try
import org.readium.r2.shared.util.asset.Asset
import org.readium.r2.shared.util.asset.AssetRetriever
import org.readium.r2.shared.util.format.FormatHints
import org.readium.r2.shared.util.http.HttpClient
import org.readium.r2.streamer.PublicationOpener
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream
import java.nio.file.Files

class BookImporterTest {

    private val tempDir = Files.createTempDirectory("test_storage").toFile()
    private val mockUrl = mockk<AbsoluteUrl>()

    private val fakeGateway = object : StorageGateway {
        override val filesDir = tempDir

        override fun openInputStream(uri: Uri): InputStream {
            return ByteArrayInputStream("Fake Book Content".toByteArray())
        }

        override fun resolveExtension(uri: Uri) = "epub"

        override fun toUrl(file: File): AbsoluteUrl {
            return mockUrl
        }
    }

    private val booksDao: BooksDao = mockk(relaxed = true)
    private val assetRetriever: AssetRetriever = mockk()
    private val publicationOpener: PublicationOpener = mockk()
    private val httpClient: HttpClient = mockk()

    private val importer = BookImporter(
        storageGateway = fakeGateway,
        booksDao = booksDao,
        assetRetriever = assetRetriever,
        publicationOpener = publicationOpener,
        httpClient = httpClient,
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
        } returns Try.success(mockAsset)

        coEvery {
            publicationOpener.open(
                asset = any<Asset>(),
                allowUserInteraction = any<Boolean>(),
            )
        } returns Try.success(mockPublication)

        coEvery { booksDao.insertBook(any()) } returns 1L

        val result = importer.importFromUri(uri = mockk(relaxed = true))


        assertTrue("Operation failed: ${result.failureOrNull()}", result.isSuccess)

        val savedBook = result.getOrNull()
        if (savedBook != null) {
            val savedFile = File(savedBook.href)
            assertTrue("File should exist in temp dir", savedFile.exists())
            assertEquals("Fake Book Content", savedFile.readText())
        }
    }
}
