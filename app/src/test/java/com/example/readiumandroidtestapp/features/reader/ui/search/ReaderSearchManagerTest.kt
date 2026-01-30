package com.example.readiumandroidtestapp.features.reader.ui.search

import androidx.paging.PagingData
import com.example.readiumandroidtestapp.features.reader.domain.SearchGateway
import com.example.readiumandroidtestapp.features.reader.ui.state.SearchItem
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.readium.r2.shared.publication.LocatorCollection
import org.readium.r2.shared.publication.Publication
import org.readium.r2.shared.publication.services.search.SearchIterator
import org.readium.r2.shared.util.Try

@OptIn(ExperimentalCoroutinesApi::class)
class ReaderSearchManagerTest {

    private val searchGateway: SearchGateway = mockk(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()
    private val manager = DefaultReaderSearchManager(searchGateway = searchGateway)

    @Test
    fun `getSearchResults emits empty PagingData initially`() = runTest(context = testDispatcher) {
        val publication = mockk<Publication>(relaxed = true)
        val results = manager.getSearchResults(
            publicationFlow = flowOf(value = publication),
            scope = backgroundScope,
        )

        val items = mutableListOf<PagingData<SearchItem>>()
        val job = launch {
            results.collect { items.add(it) }
        }

        // Wait for debounce
        advanceTimeBy(delayTimeMillis = 1000)
        advanceUntilIdle()

        assertNotNull(items.firstOrNull())
        coVerify(exactly = 0) { searchGateway.search(publication = any(), query = any()) }
        job.cancel()
    }

    @Test
    fun `getSearchResults triggers search after debounce`() = runTest(context = testDispatcher) {
        val publication = mockk<Publication>(relaxed = true)
        val query = "test query"
        val searchIterator = mockk<SearchIterator>(relaxed = true)

        // Ensure iterator returns a valid Try, otherwise PagingSource crashes
        coEvery { searchIterator.next() } returns Try.success(success = LocatorCollection(locators = emptyList()))

        coEvery {
            searchGateway.search(
                publication = publication,
                query = query,
            )
        } returns searchIterator

        val results = manager.getSearchResults(
            publicationFlow = flowOf(value = publication),
            scope = backgroundScope,
        )

        val job = launch {
            results.collect { }
        }

        manager.onSearchQueryChanged(query = query)

        // Advance time but not enough for debounce
        advanceTimeBy(delayTimeMillis = 200)
        coVerify(exactly = 0) { searchGateway.search(publication = any(), query = any()) }

        // Advance past debounce
        advanceTimeBy(delayTimeMillis = 200)
        advanceUntilIdle()

        coVerify {
            searchGateway.search(
                publication = publication,
                query = query,
            )
        }
        job.cancel()
    }

    @Test
    fun `getSearchResults does not search if publication is null`() =
        runTest(context = testDispatcher) {
            val query = "test query"

            val results = manager.getSearchResults(
                publicationFlow = flowOf(value = null),
                scope = backgroundScope,
            )

            val job = launch {
                results.collect { }
            }

            manager.onSearchQueryChanged(query = query)
            advanceTimeBy(delayTimeMillis = 1000)
            advanceUntilIdle()

            coVerify(exactly = 0) { searchGateway.search(publication = any(), query = any()) }
            job.cancel()
        }

    @Test
    fun `getSearchResults does not search if query is blank`() = runTest(context = testDispatcher) {
        val publication = mockk<Publication>(relaxed = true)
        val query = "   "

        val results = manager.getSearchResults(
            publicationFlow = flowOf(value = publication),
            scope = backgroundScope,
        )

        val job = launch {
            results.collect { }
        }

        manager.onSearchQueryChanged(query = query)
        advanceTimeBy(delayTimeMillis = 1000)
        advanceUntilIdle()

        coVerify(exactly = 0) { searchGateway.search(publication = any(), query = any()) }
        job.cancel()
    }
}
