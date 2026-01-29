package com.example.readiumandroidtestapp.core.data.opds

import org.readium.r2.shared.opds.ParseData
import org.readium.r2.shared.util.Try

interface OpdsParserLibrary {
    suspend fun parseOpds1Url(url: String): Try<ParseData, Exception>
    suspend fun parseOpds2Url(url: String): Try<ParseData, Exception>
}
