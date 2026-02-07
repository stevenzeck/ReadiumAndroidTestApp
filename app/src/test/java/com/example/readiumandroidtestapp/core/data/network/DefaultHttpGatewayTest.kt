package com.example.readiumandroidtestapp.core.data.network

import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.readium.r2.shared.util.AbsoluteUrl
import org.readium.r2.shared.util.http.DefaultHttpClient

@RunWith(AndroidJUnit4::class)
class DefaultHttpGatewayTest {

    private lateinit var server: MockWebServer
    private lateinit var gateway: DefaultHttpGateway

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        gateway =
            DefaultHttpGateway(httpClient = DefaultHttpClient())
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `fetch returns mapped result on success`() = runTest {
        val bodyContent = "content"
        server.enqueue(
            response = MockResponse()
                .setBody(body = bodyContent)
                .setHeader(name = "Content-Type", value = "application/epub+zip")
                .setResponseCode(code = 200),
        )

        val url = AbsoluteUrl(url = server.url(path = "/book.epub").toString())!!

        val result = gateway.fetch(url = url)

        assertTrue("Expected success but got failure: ${result.failureOrNull()}", result.isSuccess)
        val httpResult = result.getOrNull()

        assertEquals("application/epub+zip", httpResult?.contentType)
        assertEquals(bodyContent, String(httpResult?.body ?: ByteArray(0)))
    }

    @Test
    fun `fetch returns failure on server error`() = runTest {
        server.enqueue(response = MockResponse().setResponseCode(code = 500))
        val url = AbsoluteUrl(server.url("/error").toString())!!

        val result = gateway.fetch(url = url)

        assertTrue(result.isFailure)
    }
}
