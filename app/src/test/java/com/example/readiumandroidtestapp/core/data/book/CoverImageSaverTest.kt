package com.example.readiumandroidtestapp.core.data.book

import android.graphics.Bitmap
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.readiumandroidtestapp.core.data.storage.FakeStorageGateway
import io.mockk.coEvery
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.readium.r2.shared.publication.Manifest
import org.readium.r2.shared.publication.Metadata
import org.readium.r2.shared.publication.Publication
import org.readium.r2.shared.publication.services.cover
import java.io.File
import java.nio.file.Files

@RunWith(AndroidJUnit4::class)
class CoverImageSaverTest {

    private val tempDir = Files.createTempDirectory("robo_test").toFile()
    private val fakeGateway = FakeStorageGateway(filesDir = tempDir)
    private val saver = DefaultCoverImageSaver(storageGateway = fakeGateway)

    @Before
    fun setUp() {
        mockkStatic("org.readium.r2.shared.publication.services.CoverServiceKt")
    }

    @After
    fun tearDown() {
        unmockkStatic("org.readium.r2.shared.publication.services.CoverServiceKt")
        tempDir.deleteRecursively()
    }

    @Test
    fun `saveCover returns null if publication has no cover`() = runTest {
        val publication = Publication(manifest = Manifest(metadata = Metadata()))

        coEvery { publication.cover() } returns null

        val result = saver.saveCover(publication = publication)

        assertEquals(null, result)
    }

    @Test
    fun `saveCover saves bitmap to file and returns path`() = runTest {
        val publication = Publication(manifest = Manifest(metadata = Metadata()))
        val bitmap = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888)
        coEvery { publication.cover() } returns bitmap

        val result = saver.saveCover(publication = publication)

        assertNotNull(result)
        val file = File(result!!)

        assertTrue("File should exist", file.exists())
        assertTrue("File should have content", file.length() > 0)
        assertTrue("Path should contain 'covers/' subdir", result.contains(other = "covers/"))
    }
}
