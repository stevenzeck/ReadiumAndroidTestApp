package com.example.readiumandroidtestapp.core.data.opds

import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.readium.r2.opds.OPDS1Parser
import org.readium.r2.opds.OPDS2Parser
import org.readium.r2.shared.opds.ParseData
import org.readium.r2.shared.util.Try

@RunWith(AndroidJUnit4::class)
class DefaultOpdsParserLibraryTest {

    private val library = DefaultOpdsParserLibrary()

    @Before
    fun setUp() {
        mockkObject(OPDS1Parser)
        mockkObject(OPDS2Parser)
    }

    @After
    fun tearDown() {
        unmockkObject(OPDS1Parser)
        unmockkObject(OPDS2Parser)
    }

    @Test
    fun `parseOpds1Url calls OPDS1Parser`() = runTest {
        val url = "https://example.com/opds1"
        val expectedData = mockk<ParseData>()
        val expectedResult = Try.success(success = expectedData)

        coEvery { OPDS1Parser.parseUrlString(url = url, client = any()) } returns expectedResult

        val result = library.parseOpds1Url(url)

        assertTrue("Expected success but got $result", result.isSuccess)
        assertEquals(expectedResult, result)
    }

    @Test
    fun `parseOpds2Url calls OPDS2Parser`() = runTest {
        val url = "https://example.com/opds2"
        val expectedData = mockk<ParseData>()
        val expectedResult = Try.success(success = expectedData)

        coEvery { OPDS2Parser.parseUrlString(url = url, client = any()) } returns expectedResult

        val result = library.parseOpds2Url(url)

        assertTrue("Expected success but got $result", result.isSuccess)
        assertEquals(expectedResult, result)
    }
}
