package com.example.readiumandroidtestapp.features.reader.domain

import org.readium.r2.shared.publication.Publication
import org.readium.r2.shared.publication.services.search.SearchIterator

interface SearchGateway {
    suspend fun search(publication: Publication, query: String): SearchIterator?
}
