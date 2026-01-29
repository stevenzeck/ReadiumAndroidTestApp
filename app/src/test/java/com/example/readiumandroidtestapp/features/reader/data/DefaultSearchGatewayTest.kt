package com.example.readiumandroidtestapp.features.reader.data

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.readium.r2.shared.publication.Publication
import org.readium.r2.shared.publication.services.search.SearchIterator
import org.readium.r2.shared.publication.services.search.SearchService

class DefaultSearchGatewayTest {

    @Test
    fun `search delegates to publication search service`() = runTest {
        val gateway = DefaultSearchGateway()
        val publication: Publication = mockk()
        val searchIterator: SearchIterator = mockk()
        val searchService: SearchService = mockk()
        val query = "test"

        every { publication.findService(serviceType = SearchService::class) } returns searchService
        coEvery { searchService.search(query) } returns searchIterator

        val result = gateway.search(publication = publication, query = query)

        assertEquals(searchIterator, result)
    }
}
