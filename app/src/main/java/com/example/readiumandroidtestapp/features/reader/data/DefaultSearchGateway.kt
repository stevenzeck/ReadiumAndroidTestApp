package com.example.readiumandroidtestapp.features.reader.data

import com.example.readiumandroidtestapp.features.reader.domain.SearchGateway
import org.readium.r2.shared.publication.Publication
import org.readium.r2.shared.publication.services.search.SearchIterator
import org.readium.r2.shared.publication.services.search.search
import javax.inject.Inject

class DefaultSearchGateway @Inject constructor() : SearchGateway {
    override suspend fun search(publication: Publication, query: String): SearchIterator? {
        return publication.search(query)
    }
}
