package com.example.readiumandroidtestapp.core.data.book

import android.graphics.Bitmap
import com.example.readiumandroidtestapp.core.domain.storage.StorageGateway
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
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
import org.readium.r2.shared.publication.Publication
import org.readium.r2.shared.publication.services.cover
import org.robolectric.RobolectricTestRunner
import java.nio.file.Files

@RunWith(RobolectricTestRunner::class)
class CoverImageSaverTest {

    private val storageGateway: StorageGateway = mockk()
    private val tempDir = Files.createTempDirectory("robo_test").toFile()
    private val saver = DefaultCoverImageSaver(storageGateway = storageGateway)

    @Before
    fun setUp() {
        every { storageGateway.filesDir } returns tempDir
        mockkStatic("org.readium.r2.shared.publication.services.CoverServiceKt")
    }

    @After
    fun tearDown() {
        unmockkStatic("org.readium.r2.shared.publication.services.CoverServiceKt")
        tempDir.deleteRecursively()
    }

    @Test
    fun `saveCover returns null if publication has no cover`() = runTest {
        val publication = mockk<Publication>()
        coEvery { publication.cover() } returns null

        val result = saver.saveCover(publication = publication)

        assertEquals(null, result)
    }

    @Test
    fun `saveCover saves bitmap to file and returns path`() = runTest {
        val publication = mockk<Publication>()
        val bitmap = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888)
        coEvery { publication.cover() } returns bitmap

        val result = saver.saveCover(publication = publication)

        assertNotNull(result)
        val file = java.io.File(result!!)
        assertTrue(file.exists())
        assertTrue(file.length() > 0)
        assertTrue(result.contains("covers/"))
    }
}
