package com.example.readiumandroidtestapp.features.reader.ui.search

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.paging.AsyncPagingDataDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListUpdateCallback
import com.example.readiumandroidtestapp.features.reader.domain.SearchGateway
import com.example.readiumandroidtestapp.features.reader.ui.state.SearchItem
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.readium.r2.navigator.Decoration
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

        backgroundScope.launch(context = UnconfinedTestDispatcher(testScheduler)) {
            results.collect()
        }
        advanceUntilIdle()
    }

    @Test
    fun `getSearchResults calls gateway and updates searchDecorations`() = runTest {
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

        // Monitor decorations
        val decorationsEmitted = mutableListOf<List<Decoration>>()
        backgroundScope.launch(context = UnconfinedTestDispatcher(scheduler = testScheduler)) {
            manager.searchDecorations.collect { decorationsEmitted.add(it) }
        }

        val differ = AsyncPagingDataDiffer(
            diffCallback = object : DiffUtil.ItemCallback<SearchItem>() {
                override fun areItemsTheSame(oldItem: SearchItem, newItem: SearchItem): Boolean =
                    oldItem == newItem

                override fun areContentsTheSame(oldItem: SearchItem, newItem: SearchItem): Boolean =
                    oldItem == newItem
            },
            updateCallback = object : ListUpdateCallback {
                override fun onInserted(position: Int, count: Int) {}
                override fun onRemoved(position: Int, count: Int) {}
                override fun onMoved(fromPosition: Int, toPosition: Int) {}
                override fun onChanged(position: Int, count: Int, payload: Any?) {}
            },
            workerDispatcher = UnconfinedTestDispatcher(scheduler = testScheduler),
        )

        manager.onSearchQueryChanged(query = "query")

        backgroundScope.launch(context = UnconfinedTestDispatcher(scheduler = testScheduler)) {
            results.collectLatest {
                differ.submitData(pagingData = it)
            }
        }

        advanceTimeBy(delayTimeMillis = 1000)
        advanceUntilIdle()

        coVerify { searchGateway.search(publication = publication, query = "query") }
        coVerify { iterator.next() }

        val hasDecorations = decorationsEmitted.any { it.isNotEmpty() }
        assertTrue(
            "Expected decorations to be emitted. Emitted: $decorationsEmitted",
            hasDecorations,
        )

        val lastDecorations = decorationsEmitted.last()
        assertEquals(1, lastDecorations.size)
        val decoration = lastDecorations[0]
        assertEquals(locator, decoration.locator)
        assertTrue(decoration.style is Decoration.Style.Underline)
        assertEquals(Color.Red.toArgb(), (decoration.style as Decoration.Style.Underline).tint)
    }
}
