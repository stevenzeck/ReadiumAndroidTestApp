package com.example.readiumandroidtestapp.core.data.opds

import org.readium.r2.opds.OPDS1Parser
import org.readium.r2.opds.OPDS2Parser
import org.readium.r2.shared.opds.ParseData
import org.readium.r2.shared.util.Try
import javax.inject.Inject

class DefaultOpdsParserLibrary @Inject constructor() : OpdsParserLibrary {
    override suspend fun parseOpds1Url(url: String): Try<ParseData, Exception> {
        return OPDS1Parser.parseUrlString(url = url)
    }

    override suspend fun parseOpds2Url(url: String): Try<ParseData, Exception> {
        return OPDS2Parser.parseUrlString(url = url)
    }
}
