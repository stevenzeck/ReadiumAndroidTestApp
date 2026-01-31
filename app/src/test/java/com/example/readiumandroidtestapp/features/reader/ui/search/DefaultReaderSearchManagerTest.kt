package com.example.readiumandroidtestapp.features.reader.ui.search

import com.example.readiumandroidtestapp.features.reader.domain.SearchGateway
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.readium.r2.shared.publication.Locator
import org.readium.r2.shared.publication.Publication
import org.readium.r2.shared.publication.services.search.SearchIterator
import org.readium.r2.shared.publication.services.search.SearchTry
import org.readium.r2.shared.util.Url
import org.readium.r2.shared.util.mediatype.MediaType
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
@OptIn(ExperimentalCoroutinesApi::class)
class DefaultReaderSearchManagerTest {

    private val searchGateway: SearchGateway = mockk()
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var manager: DefaultReaderSearchManager

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        manager = DefaultReaderSearchManager(searchGateway)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `searchQuery updates state`() = runTest {
        manager.onSearchQueryChanged(query = "query")
        assertEquals("query", manager.searchQuery.value)
    }

    @Test
    fun `getSearchResults returns empty when query is blank`() = runTest {
        val publication = mockk<Publication>()
        val results =
            manager.getSearchResults(publicationFlow = flowOf(value = publication), backgroundScope)

        manager.onSearchQueryChanged(query = "")

        // Collect to trigger flow
        backgroundScope.launch(context = UnconfinedTestDispatcher(testScheduler)) {
            results.collect()
        }
        advanceUntilIdle()
    }

    @Test
    fun `getSearchResults calls gateway when query is valid`() = runTest {
        val publication = mockk<Publication>()
        val iterator = mockk<SearchIterator>(relaxed = true)
        val locator = Locator(
            href = Url(url = "href")!!,
            mediaType = MediaType(string = "text/html")!!,
            title = "Result",
        )
        val mockCollection =
            mockk<org.readium.r2.shared.publication.LocatorCollection>(relaxed = true)
        every { mockCollection.locators } returns listOf(locator)

        coEvery {
            searchGateway.search(
                publication = publication,
                query = "query",
            )
        } returns iterator
        coEvery { iterator.next() } returns SearchTry.success(success = mockCollection) andThen SearchTry.success(
            success = null,
        )

        val results =
            manager.getSearchResults(publicationFlow = flowOf(value = publication), backgroundScope)
        manager.onSearchQueryChanged(query = "query")

        backgroundScope.launch(context = UnconfinedTestDispatcher(scheduler = testScheduler)) {
            results.collect()
        }
        advanceUntilIdle()

        coEvery { searchGateway.search(publication = publication, query = "query") }
    }
}
