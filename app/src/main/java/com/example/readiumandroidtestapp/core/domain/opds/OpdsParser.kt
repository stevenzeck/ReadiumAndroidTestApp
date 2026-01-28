package com.example.readiumandroidtestapp.core.domain.opds

import org.readium.r2.shared.opds.ParseData
import org.readium.r2.shared.util.Try

interface OpdsParser {
    suspend fun parseUrlString(url: String): Try<ParseData, Exception>
}
