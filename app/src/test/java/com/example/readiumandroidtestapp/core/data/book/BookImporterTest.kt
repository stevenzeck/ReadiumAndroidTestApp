package com.example.readiumandroidtestapp.core.data.book

import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.readiumandroidtestapp.core.data.database.BooksDao
import com.example.readiumandroidtestapp.core.data.storage.StorageManager
import com.example.readiumandroidtestapp.core.domain.model.Book
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.readium.r2.shared.publication.Publication
import org.readium.r2.shared.util.AbsoluteUrl
import org.readium.r2.shared.util.Try
import org.readium.r2.shared.util.asset.Asset
import org.readium.r2.shared.util.asset.AssetRetriever
import org.readium.r2.shared.util.http.DefaultHttpClient
import org.readium.r2.shared.util.mediatype.MediaType
import org.readium.r2.streamer.PublicationOpener
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class BookImporterTest {

    private lateinit var context: Context
    private lateinit var storageManager: StorageManager
    private lateinit var server: MockWebServer

    private val booksDao: BooksDao = mockk(relaxed = true)
    private val assetRetriever: AssetRetriever = mockk()
    private val publicationOpener: PublicationOpener = mockk()
    private val coverImageSaver: CoverImageSaver = mockk()

    private lateinit var importer: BookImporter

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()

        context = ApplicationProvider.getApplicationContext()
        storageManager = StorageManager(context = context)

        importer = BookImporter(
            storageManager = storageManager,
            booksDao = booksDao,
            assetRetriever = assetRetriever,
            publicationOpener = publicationOpener,
            httpClient = DefaultHttpClient(),
            coverImageSaver = coverImageSaver,
            ioDispatcher = UnconfinedTestDispatcher(),
        )
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `importFromUri saves file and inserts book into database`() = runTest {
        // Arrange
        val testFile = File(context.cacheDir, "source_book.epub")
        testFile.writeText(text = "Local Book Content")
        val uri = Uri.fromFile(testFile)

        val mockAsset = mockk<Asset>()
        every { mockAsset.format.mediaType } returns MediaType.EPUB

        val mockPublication = mockk<Publication>(relaxed = true)
        every { mockPublication.metadata.title } returns "My Local Book"

        coEvery {
            assetRetriever.retrieve(url = any())
        } returns Try.success(success = mockAsset)

        coEvery {
            publicationOpener.open(asset = mockAsset, allowUserInteraction = false)
        } returns Try.success(success = mockPublication)

        coEvery { coverImageSaver.saveCover(publication = mockPublication) } returns null
        coEvery { booksDao.insertBook(book = any()) } returns 1L

        // Act
        val result = importer.importFromUri(uri = uri)

        // Assert
        assertTrue("Result should be success", result.isSuccess)

        val savedBook = result.getOrNull()!!
        val savedFile = File(savedBook.href)
        assertTrue("Saved file should exist", savedFile.exists())
        assertEquals(
            "File content should match original",
            "Local Book Content",
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
        server.enqueue(
            response = MockResponse()
                .setBody(body = remoteContent)
                .setHeader(name = "Content-Type", value = "application/epub+zip")
                .setResponseCode(code = 200),
        )

        val mockAsset = mockk<Asset>()
        every { mockAsset.format.mediaType } returns MediaType.EPUB

        val mockPublication = mockk<Publication>(relaxed = true)
        every { mockPublication.metadata.title } returns "Remote Book"

        coEvery {
            assetRetriever.retrieve(url = any())
        } returns Try.success(success = mockAsset)

        coEvery {
            publicationOpener.open(asset = mockAsset, allowUserInteraction = false)
        } returns Try.success(success = mockPublication)

        coEvery { coverImageSaver.saveCover(publication = mockPublication) } returns null
        coEvery { booksDao.insertBook(book = any()) } returns 1L

        // Act
        val remoteUrl = AbsoluteUrl(url = server.url(path = "/book.epub").toString())!!
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
