package com.example.readiumandroidtestapp.core.domain.opds

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.readiumandroidtestapp.core.domain.model.Catalog
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.readium.r2.opds.OPDS1Parser
import org.readium.r2.opds.OPDS2Parser
import org.readium.r2.shared.opds.Feed
import org.readium.r2.shared.opds.ParseData
import org.readium.r2.shared.util.Try
import org.readium.r2.shared.util.Url

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class OpdsParserTest {

    private val parser = OpdsParser()

    @Before
    fun setUp() {
        mockkObject(OPDS1Parser)
        mockkObject(OPDS2Parser)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `parseUrlString with OPDS 1 type delegates to OPDS 1 parser`() = runTest {
        val url = "http://example.com/opds1"
        val feed = Feed(title = "OPDS 1 Feed", type = 1, href = Url(url = url)!!)
        val expectedData = ParseData(feed = feed, publication = null, type = 1)

        coEvery { OPDS1Parser.parseUrlString(url = url, client = any()) } returns Try.success(
            success = expectedData,
        )

        val result = parser.parseUrlString(url = url, type = Catalog.TYPE_OPDS_1)

        assertTrue(result.isSuccess)
        assertEquals(expectedData, result.getOrNull())
        coVerify(exactly = 0) { OPDS2Parser.parseUrlString(any(), any()) }
    }

    @Test
    fun `parseUrlString with OPDS 2 type delegates to OPDS 2 parser`() = runTest {
        val url = "http://example.com/opds2"
        val feed = Feed(title = "OPDS 2 Feed", type = 1, href = Url(url = url)!!)
        val expectedData = ParseData(feed = feed, publication = null, type = 2)

        coEvery { OPDS2Parser.parseUrlString(url = url, client = any()) } returns Try.success(
            success = expectedData,
        )

        val result = parser.parseUrlString(url = url, type = Catalog.TYPE_OPDS_2)

        assertTrue(result.isSuccess)
        assertEquals(expectedData, result.getOrNull())
        coVerify(exactly = 0) { OPDS1Parser.parseUrlString(any(), any()) }
    }

    @Test
    fun `parseUrlString with unknown type tries OPDS 2 then OPDS 1`() = runTest {
        val url = "http://example.com/unknown"
        val feed = Feed(title = "Fallback Feed", type = 1, href = Url(url = url)!!)
        val expectedData = ParseData(feed = feed, publication = null, type = 1)

        coEvery { OPDS2Parser.parseUrlString(url = url, client = any()) } returns Try.failure(
            failure = Exception("Not OPDS 2"),
        )
        coEvery { OPDS1Parser.parseUrlString(url = url, client = any()) } returns Try.success(
            success = expectedData,
        )

        val result = parser.parseUrlString(url = url, type = null)

        assertTrue(result.isSuccess)
        assertEquals(expectedData, result.getOrNull())
        coVerify { OPDS2Parser.parseUrlString(url = url, client = any()) }
        coVerify { OPDS1Parser.parseUrlString(url = url, client = any()) }
    }

    @Test
    fun `parseUrlString with unknown type returns OPDS 2 success immediately`() = runTest {
        val url = "http://example.com/unknown"

        val feed = Feed(title = "Immediate Feed", type = 1, href = Url(url = url)!!)
        val expectedData = ParseData(feed = feed, publication = null, type = 2)

        coEvery { OPDS2Parser.parseUrlString(url = url, client = any()) } returns Try.success(
            success = expectedData,
        )

        val result = parser.parseUrlString(url = url, type = null)

        assertTrue(result.isSuccess)
        assertEquals(expectedData, result.getOrNull())
        coVerify { OPDS2Parser.parseUrlString(url = url, client = any()) }
        coVerify(exactly = 0) { OPDS1Parser.parseUrlString(url = any(), client = any()) }
    }
}
