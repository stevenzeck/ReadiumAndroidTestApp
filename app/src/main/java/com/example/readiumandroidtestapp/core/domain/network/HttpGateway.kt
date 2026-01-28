package com.example.readiumandroidtestapp.core.domain.network

import org.readium.r2.shared.util.AbsoluteUrl
import org.readium.r2.shared.util.Try

data class HttpResult(
    val body: ByteArray,
    val contentType: String?,
) {
    // Being a ByteArray, need to override to check for content equality.
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as HttpResult

        if (!body.contentEquals(other.body)) return false
        if (contentType != other.contentType) return false

        return true
    }

    override fun hashCode(): Int {
        var result = body.contentHashCode()
        result = 31 * result + (contentType?.hashCode() ?: 0)
        return result
    }
}

interface HttpGateway {
    suspend fun fetch(url: AbsoluteUrl): Try<HttpResult, Exception>
}
