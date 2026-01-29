package com.example.readiumandroidtestapp.core.data.opds

import com.example.readiumandroidtestapp.core.domain.model.Catalog
import com.example.readiumandroidtestapp.core.domain.opds.OpdsParser
import org.readium.r2.shared.opds.ParseData
import org.readium.r2.shared.util.Try
import javax.inject.Inject

class DefaultOpdsParser @Inject constructor(
    private val opdsParserLibrary: OpdsParserLibrary,
) : OpdsParser {

    override suspend fun parseUrlString(url: String, type: Int?): Try<ParseData, Exception> {
        return when (type) {
            Catalog.TYPE_OPDS_1 -> opdsParserLibrary.parseOpds1Url(url = url)
            Catalog.TYPE_OPDS_2 -> opdsParserLibrary.parseOpds2Url(url = url)
            else -> {
                opdsParserLibrary.parseOpds2Url(url = url).onFailure {
                    return opdsParserLibrary.parseOpds1Url(url = url)
                }
            }
        }
    }
}
