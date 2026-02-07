package com.example.readiumandroidtestapp.core.data.storage

import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.readium.r2.shared.util.toUrl
import java.io.ByteArrayInputStream
import java.io.File
import java.io.IOException
import java.io.InputStream

@RunWith(AndroidJUnit4::class)
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
    fun `openInputStream returns stream for valid file uri`() {
        val file = File(context.cacheDir, "test_input.txt")
        file.writeText("test content")
        val uri = Uri.fromFile(file)

        val stream = storageGateway.openInputStream(uri = uri)
        assertNotNull(stream)
        assertEquals("test content", stream?.bufferedReader()?.readText())
        stream?.close()
    }

    @Test
    fun `resolveExtension returns correct extension for file scheme`() {
        val uri = Uri.parse("file:///path/to/book.epub")
        val extension = storageGateway.resolveExtension(uri = uri)
        assertEquals("epub", extension)
    }

    @Test
    fun `resolveExtension returns empty string for file scheme without extension`() {
        // MimeTypeMap.getFileExtensionFromUrl returns "" for no extension
        val uri = Uri.parse("file:///path/to/book")
        val extension = storageGateway.resolveExtension(uri = uri)
        assertEquals("", extension)
    }

    @Test
    fun `resolveExtension returns default epub for content scheme with unknown mime type`() {
        val uri = Uri.parse("content://com.example/unknown")
        val extension = storageGateway.resolveExtension(uri = uri)
        assertEquals("epub", extension)
    }

    @Test
    fun `resolveExtensionFromMimeType returns extension for known mime type`() {
        val extension = storageGateway.resolveExtensionFromMimeType(mimeType = "application/pdf")
        assertEquals("pdf", extension)
    }

    @Test
    fun `resolveExtensionFromMimeType returns null for unknown mime type`() {
        val extension =
            storageGateway.resolveExtensionFromMimeType(mimeType = "application/x-unknown")
        assertNull(extension)
    }

    @Test
    fun `toUrl converts file to AbsoluteUrl`() {
        val file = File("/path/to/file.epub")
        val expectedUrl = file.toUrl()
        assertEquals(expectedUrl, storageGateway.toUrl(file = file))
    }

    @Test
    fun `saveFileFromStream saves content to file with extension`() {
        val content = "Hello World"
        val stream = ByteArrayInputStream(content.toByteArray())
        val extension = "txt"

        val result = storageGateway.saveFileFromStream(input = stream, extension = extension)

        assertTrue(result.isSuccess)
        val file = result.getOrNull()!!
        assertTrue(file.exists())
        assertTrue(file.name.endsWith(suffix = ".txt"))
        assertEquals(content, file.readText())

        file.delete()
    }

    @Test
    fun `saveFileFromStream saves content to file with dotted extension`() {
        val content = "Hello World"
        val stream = ByteArrayInputStream(content.toByteArray())
        val extension = ".epub"

        val result = storageGateway.saveFileFromStream(input = stream, extension = extension)

        assertTrue(result.isSuccess)
        val file = result.getOrNull()!!
        assertTrue(file.name.endsWith(suffix = ".epub"))
        file.delete()
    }

    @Test
    fun `saveFileFromStream saves content to file with null extension`() {
        val content = "Hello World"
        val stream = ByteArrayInputStream(content.toByteArray())

        val result = storageGateway.saveFileFromStream(input = stream, extension = null)

        assertTrue(result.isSuccess)
        val file = result.getOrNull()!!
        assertTrue(file.name.endsWith(suffix = ".epub"))
        file.delete()
    }

    @Test
    fun `saveFileFromStream returns failure on exception`() {
        val failingStream = object : InputStream() {
            override fun read(): Int = throw IOException("Read error")
        }

        val result = storageGateway.saveFileFromStream(input = failingStream, extension = "txt")

        assertTrue(result.isFailure)
        assertTrue(result.failureOrNull() is IOException)
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

    @Test
    fun `deleteFile returns false for non-existing file`() {
        val result = storageGateway.deleteFile(path = "/non/existing/path")
        assertTrue(!result)
    }
}
