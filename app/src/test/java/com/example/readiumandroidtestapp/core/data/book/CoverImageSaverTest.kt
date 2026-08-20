package com.example.readiumandroidtestapp.core.data.book

import android.content.Context
import android.graphics.Bitmap
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.readiumandroidtestapp.core.data.storage.StorageManager
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.readium.r2.shared.publication.Manifest
import org.readium.r2.shared.publication.Metadata
import org.readium.r2.shared.publication.Publication
import org.readium.r2.shared.publication.services.CoverService
import org.readium.r2.shared.util.http.HttpClient
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class CoverImageSaverTest {

    private lateinit var context: Context
    private lateinit var storageManager: StorageManager
    private val httpClient: HttpClient = mockk(relaxed = true)
    private lateinit var saver: CoverImageSaver

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        storageManager = StorageManager(context = context)
        saver = CoverImageSaver(
            storageManager = storageManager,
            httpClient = httpClient,
            ioDispatcher = UnconfinedTestDispatcher(),
        )
    }

    @Test
    fun `saveCover returns null if publication has no cover`() = runTest {
        val publication = Publication(manifest = Manifest(metadata = Metadata()))

        val result = saver.saveCover(publication = publication)

        assertEquals(null, result)
    }

    @Test
    fun `saveCover saves bitmap to file and returns path`() = runTest {
        val bitmap = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888)
        val coverService = object : CoverService {
            override suspend fun cover(): Bitmap = bitmap
        }

        val publication = Publication(
            manifest = Manifest(metadata = Metadata()),
            servicesBuilder = Publication.ServicesBuilder(
                cover = { _ -> coverService },
            ),
        )

        val result = saver.saveCover(publication = publication)

        assertNotNull(result)
        val file = File(result!!)

        assertTrue("File should exist", file.exists())
        assertTrue("File should have content", file.length() > 0)
        assertTrue("Path should contain 'covers/' subdir", result.contains(other = "covers/"))
    }
}
