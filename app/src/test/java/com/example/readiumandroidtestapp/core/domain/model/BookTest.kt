package com.example.readiumandroidtestapp.core.domain.model

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.readium.r2.shared.util.mediatype.MediaType

@RunWith(AndroidJUnit4::class)
class BookTest {

    @Test
    fun `secondary constructor correctly maps properties`() {
        val id = 123L
        val creation = 456L
        val href = "path/to/book.epub"
        val title = "Test Book"
        val author = "Test Author"
        val identifier = "isbn-123"
        val progression = "{}"
        val mediaType = MediaType.EPUB
        val cover = "path/to/cover.jpg"

        val book = Book(
            id = id,
            creation = creation,
            href = href,
            title = title,
            author = author,
            identifier = identifier,
            progression = progression,
            mediaType = mediaType,
            cover = cover,
        )

        assertEquals(id, book.id)
        assertEquals(creation, book.creation)
        assertEquals(href, book.href)
        assertEquals(title, book.title)
        assertEquals(author, book.author)
        assertEquals(identifier, book.identifier)
        assertEquals(progression, book.progression)
        assertEquals(mediaType.toString(), book.rawMediaType)
        assertEquals(cover, book.cover)
    }

    @Test
    fun `url property adds file scheme for local paths`() {
        val book = Book(
            href = "/storage/emulated/0/Books/test.epub",
            title = "Test",
            identifier = "id",
            rawMediaType = "application/epub+zip",
            cover = null,
        )

        val url = book.url
        assertNotNull(url)
        assertEquals("file:///storage/emulated/0/Books/test.epub", url.toString())
    }

    @Test
    fun `url property keeps existing scheme`() {
        val book = Book(
            href = "http://example.com/book.epub",
            title = "Test",
            identifier = "id",
            rawMediaType = "application/epub+zip",
            cover = null,
        )

        val url = book.url
        assertNotNull(url)
        assertEquals("http://example.com/book.epub", url.toString())
    }

    @Test
    fun `mediaType property parses rawMediaType`() {
        val book = Book(
            href = "href",
            title = "Test",
            identifier = "id",
            rawMediaType = "application/epub+zip",
            cover = null,
        )

        val mediaType = book.mediaType
        assertNotNull(mediaType)
        assertEquals(MediaType.EPUB, mediaType)
    }
}
