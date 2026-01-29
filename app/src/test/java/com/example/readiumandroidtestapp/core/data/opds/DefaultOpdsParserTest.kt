package com.example.readiumandroidtestapp.core.data.opds

import com.example.readiumandroidtestapp.core.domain.model.Catalog
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.readium.r2.shared.opds.ParseData
import org.readium.r2.shared.util.Try

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultOpdsParserTest {

    private val opdsParserLibrary: OpdsParserLibrary = mockk()
    private val parser = DefaultOpdsParser(opdsParserLibrary = opdsParserLibrary)

    @Test
    fun `parseUrlString with OPDS 1 type delegates to OPDS 1 parser`() = runTest {
        val url = "http://example.com/opds1"
        val expectedData = mockk<ParseData>()
        coEvery { opdsParserLibrary.parseOpds1Url(url = url) } returns Try.success(success = expectedData)

        val result = parser.parseUrlString(url = url, type = Catalog.TYPE_OPDS_1)

        assertTrue(result.isSuccess)
        assertEquals(expectedData, result.getOrNull())
        coVerify(exactly = 0) { opdsParserLibrary.parseOpds2Url(any()) }
    }

    @Test
    fun `parseUrlString with OPDS 2 type delegates to OPDS 2 parser`() = runTest {
        val url = "http://example.com/opds2"
        val expectedData = mockk<ParseData>()
        coEvery { opdsParserLibrary.parseOpds2Url(url = url) } returns Try.success(success = expectedData)

        val result = parser.parseUrlString(url = url, type = Catalog.TYPE_OPDS_2)

        assertTrue(result.isSuccess)
        assertEquals(expectedData, result.getOrNull())
        coVerify(exactly = 0) { opdsParserLibrary.parseOpds1Url(any()) }
    }

    @Test
    fun `parseUrlString with unknown type tries OPDS 2 then OPDS 1`() = runTest {
        val url = "http://example.com/unknown"
        val expectedData = mockk<ParseData>()

        // OPDS 2 fails
        coEvery { opdsParserLibrary.parseOpds2Url(url = url) } returns Try.failure(
            failure = Exception(
                "Not OPDS 2",
            ),
        )
        // OPDS 1 succeeds
        coEvery { opdsParserLibrary.parseOpds1Url(url = url) } returns Try.success(success = expectedData)

        val result = parser.parseUrlString(url = url, type = null)

        assertTrue(result.isSuccess)
        assertEquals(expectedData, result.getOrNull())
        coVerify { opdsParserLibrary.parseOpds2Url(url = url) }
        coVerify { opdsParserLibrary.parseOpds1Url(url = url) }
    }

    @Test
    fun `parseUrlString with unknown type returns OPDS 2 success immediately`() = runTest {
        val url = "http://example.com/unknown"
        val expectedData = mockk<ParseData>()

        // OPDS 2 succeeds
        coEvery { opdsParserLibrary.parseOpds2Url(url = url) } returns Try.success(success = expectedData)

        val result = parser.parseUrlString(url = url, type = null)

        assertTrue(result.isSuccess)
        assertEquals(expectedData, result.getOrNull())
        coVerify { opdsParserLibrary.parseOpds2Url(url = url) }
        coVerify(exactly = 0) { opdsParserLibrary.parseOpds1Url(url = any()) }
    }
}
