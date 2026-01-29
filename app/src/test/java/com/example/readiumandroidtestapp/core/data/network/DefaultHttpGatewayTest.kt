package com.example.readiumandroidtestapp.core.data.network

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.readium.r2.shared.util.AbsoluteUrl
import org.readium.r2.shared.util.DebugError
import org.readium.r2.shared.util.Try
import org.readium.r2.shared.util.http.HttpClient
import org.readium.r2.shared.util.http.HttpError
import org.readium.r2.shared.util.http.HttpRequest
import org.readium.r2.shared.util.http.HttpResponse
import org.readium.r2.shared.util.http.HttpStatus
import org.readium.r2.shared.util.http.HttpStreamResponse
import org.readium.r2.shared.util.mediatype.MediaType
import java.io.ByteArrayInputStream

class DefaultHttpGatewayTest {

    private val httpClient: HttpClient = mockk()
    private val gateway = DefaultHttpGateway(httpClient = httpClient)

    @Test
    fun `fetch returns mapped result on success`() = runTest {
        val url = mockk<AbsoluteUrl>()
        val bodyBytes = "content".toByteArray()
        val mediaType = MediaType("application/epub+zip")

        val httpResponse = HttpResponse(
            request = HttpRequest(url = url),
            url = url,
            statusCode = HttpStatus.Success,
            headers = mapOf("Content-Type" to listOf("application/epub+zip")),
            mediaType = mediaType,
        )

        coEvery { httpClient.stream(request = any()) } returns Try.success(
            success = HttpStreamResponse(
                response = httpResponse,
                body = ByteArrayInputStream(bodyBytes),
            ),
        )

        val result = gateway.fetch(url = url)

        assertTrue(result.isSuccess)
        val httpResult = result.getOrNull()
        assertEquals("application/epub+zip", httpResult?.contentType)
        // ByteArray comparison needs to be content-based
        assertEquals(String(bodyBytes), String(httpResult?.body ?: ByteArray(0)))
    }

    @Test
    fun `fetch returns failure on exception`() = runTest {
        val url = mockk<AbsoluteUrl>()
        val error = HttpError.Unreachable(cause = DebugError("Network error"))

        coEvery { httpClient.stream(request = any()) } returns Try.failure(failure = error)

        val result = gateway.fetch(url = url)

        assertTrue(result.isFailure)
        assertEquals("Server could not be reached.", result.failureOrNull()?.message)
    }
}
