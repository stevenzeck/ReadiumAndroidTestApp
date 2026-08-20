package com.example.readiumandroidtestapp.core.domain.opds

import com.example.readiumandroidtestapp.core.domain.model.Catalog
import org.readium.r2.opds.OPDS1Parser
import org.readium.r2.opds.OPDS2Parser
import org.readium.r2.shared.opds.ParseData
import org.readium.r2.shared.util.Try
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OpdsParser @Inject constructor() {
    suspend fun parseUrlString(url: String, type: Int? = null): Try<ParseData, Exception> {
        return when (type) {
            Catalog.TYPE_OPDS_1 -> OPDS1Parser.parseUrlString(url = url)
            Catalog.TYPE_OPDS_2 -> OPDS2Parser.parseUrlString(url = url)
            else -> {
                OPDS2Parser.parseUrlString(url = url).onFailure {
                    return OPDS1Parser.parseUrlString(url = url)
                }
            }
        }
    }
}
