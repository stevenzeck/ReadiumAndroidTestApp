package com.example.readiumandroidtestapp.core.data.opds

import com.example.readiumandroidtestapp.core.domain.opds.OpdsParser
import org.readium.r2.opds.OPDS1Parser
import org.readium.r2.opds.OPDS2Parser
import org.readium.r2.shared.opds.ParseData
import org.readium.r2.shared.util.Try
import javax.inject.Inject

class DefaultOpdsParser @Inject constructor() : OpdsParser {

    override suspend fun parseUrlString(url: String): Try<ParseData, Exception> {
        return OPDS2Parser.parseUrlString(url = url).onFailure {
            return OPDS1Parser.parseUrlString(url = url)
        }
    }
}
