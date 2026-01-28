package com.example.readiumandroidtestapp.core.data.network

import com.example.readiumandroidtestapp.core.domain.network.HttpGateway
import com.example.readiumandroidtestapp.core.domain.network.HttpResult
import org.readium.r2.shared.util.AbsoluteUrl
import org.readium.r2.shared.util.Try
import org.readium.r2.shared.util.http.HttpClient
import org.readium.r2.shared.util.http.HttpRequest
import org.readium.r2.shared.util.http.fetch
import javax.inject.Inject

class DefaultHttpGateway @Inject constructor(
    private val httpClient: HttpClient,
) : HttpGateway {

    override suspend fun fetch(url: AbsoluteUrl): Try<HttpResult, Exception> {
        return httpClient.fetch(HttpRequest(url))
            .map { response ->
                val contentType = response.response.headers["Content-Type"]
                    ?: response.response.headers["content-type"]
                val mimeType = contentType?.firstOrNull()?.substringBefore(";")?.trim()

                HttpResult(
                    body = response.body,
                    contentType = mimeType,
                )
            }
            .mapFailure { exception ->
                Exception(exception.message)
            }
    }
}
