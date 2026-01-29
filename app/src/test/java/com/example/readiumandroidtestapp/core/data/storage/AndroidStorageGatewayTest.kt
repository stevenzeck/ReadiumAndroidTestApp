package com.example.readiumandroidtestapp.core.data.storage

import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.ByteArrayInputStream
import java.io.File

@RunWith(RobolectricTestRunner::class)
class AndroidStorageGatewayTest {

    private lateinit var context: Context
    private lateinit var storageGateway: AndroidStorageGateway

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        storageGateway = AndroidStorageGateway(context = context)
    }

    @Test
    fun `filesDir returns context filesDir`() {
        assertEquals(context.filesDir, storageGateway.filesDir)
    }

    @Test
    fun `resolveExtension returns correct extension for file scheme`() {
        val uri = Uri.parse("file:///path/to/book.epub")
        val extension = storageGateway.resolveExtension(uri = uri)
        assertEquals("epub", extension)
    }

    @Test
    fun `saveFileFromStream saves content to file`() {
        val content = "Hello World"
        val stream = ByteArrayInputStream(content.toByteArray())
        val extension = ".txt"

        val result = storageGateway.saveFileFromStream(input = stream, extension = extension)

        assertTrue(result.isSuccess)
        val file = result.getOrNull()!!
        assertTrue(file.exists())
        assertTrue(file.name.endsWith(suffix = ".txt"))
        assertEquals(content, file.readText())

        // Cleanup
        file.delete()
    }

    @Test
    fun `deleteFile deletes existing file`() {
        val file = File(context.filesDir, "test_delete.txt")
        file.writeText(text = "content")
        assertTrue(file.exists())

        val result = storageGateway.deleteFile(path = file.absolutePath)

        assertTrue(result)
        assertTrue(!file.exists())
    }
}
