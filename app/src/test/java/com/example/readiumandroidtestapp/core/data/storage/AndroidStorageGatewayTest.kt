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
        file.writeText(text = "test content")
        val uri = Uri.fromFile(file)

        val stream = storageGateway.openInputStream(uri = uri)

        assertNotNull("Stream should not be null", stream)
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
        val uri = Uri.parse("file:///path/to/book")
        val extension = storageGateway.resolveExtension(uri = uri)
        assertEquals("", extension)
    }

    @Test
    fun `resolveExtensionFromMimeType returns extension for known mime type`() {
        val extension = storageGateway.resolveExtensionFromMimeType(mimeType = "application/pdf")
        assertEquals("pdf", extension)
    }

    @Test
    fun `resolveExtensionFromMimeType returns null for unknown mime type`() {
        val extension =
            storageGateway.resolveExtensionFromMimeType(mimeType = "application/x-unknown-format-123")
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

        val result = storageGateway.saveFileFromStream(input = stream, extension = "txt")

        assertTrue(result.isSuccess)
        val file = result.getOrNull()!!

        assertTrue("File should exist", file.exists())
        assertTrue("File name should end with .txt", file.name.endsWith(suffix = ".txt"))
        assertEquals("Content should match", content, file.readText())

        file.delete()
    }

    @Test
    fun `saveFileFromStream handles failing input stream`() {
        val failingStream = object : InputStream() {
            override fun read(): Int = throw IOException("Simulated Read Error")
        }

        val result = storageGateway.saveFileFromStream(input = failingStream, extension = "txt")

        assertTrue("Should return failure", result.isFailure)
        assertTrue("Exception should be IOException", result.failureOrNull() is IOException)
    }

    @Test
    fun `deleteFile deletes existing file`() {
        val file = File(context.filesDir, "test_delete.txt")
        file.writeText(text = "content")
        assertTrue(file.exists())

        val result = storageGateway.deleteFile(path = file.absolutePath)

        assertTrue("Delete should return true", result)
        assertTrue("File should be gone", !file.exists())
    }

    @Test
    fun `deleteFile returns false for non-existing file`() {
        val result = storageGateway.deleteFile(path = "/data/data/com.example/does_not_exist.txt")
        assertTrue("Should return false if file didn't exist", !result)
    }
}
