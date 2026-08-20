package com.example.readiumandroidtestapp.features.reader.ui.search

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.paging.AsyncPagingDataDiffer
import androidx.paging.PagingData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListUpdateCallback
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.readiumandroidtestapp.features.reader.ui.state.SearchItem
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.readium.r2.shared.publication.Locator
import org.readium.r2.shared.publication.LocatorCollection
import org.readium.r2.shared.publication.Publication
import org.readium.r2.shared.publication.services.search.SearchIterator
import org.readium.r2.shared.publication.services.search.search
import org.readium.r2.shared.util.Try
import org.readium.r2.shared.util.Url
import org.readium.r2.shared.util.mediatype.MediaType

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class ReaderSearchManagerTest {

    private val manager = ReaderSearchManager()
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher = testDispatcher)
        mockkStatic("org.readium.r2.shared.publication.services.search.SearchServiceKt")
    }

    @After
    fun tearDown() {
        unmockkStatic("org.readium.r2.shared.publication.services.search.SearchServiceKt")
        Dispatchers.resetMain()
    }

    @Test
    fun `onSearchQueryChanged updates searchQuery`() = runTest {
        manager.onSearchQueryChanged(query = "test")
        assertEquals("test", manager.searchQuery.value)
    }

    @Test
    fun `getSearchResults returns empty when query is null`() = runTest {
        val publicationFlow = MutableStateFlow<Publication?>(value = mockk())
        val results =
            manager.getSearchResults(publicationFlow = publicationFlow, scope = backgroundScope)

        val pagingData = results.first()
        val items = collectItems(pagingData = pagingData)
        assertTrue(items.isEmpty())
    }

    @Test
    fun `getSearchResults returns empty when search is empty string`() = runTest {
        manager.onSearchQueryChanged(query = "")
        val publicationFlow = MutableStateFlow<Publication?>(value = mockk())
        val results =
            manager.getSearchResults(publicationFlow = publicationFlow, scope = backgroundScope)

        val pagingData = results.first()
        val items = collectItems(pagingData = pagingData)
        assertTrue(items.isEmpty())
    }

    @Test
    fun `getSearchResults returns empty when publication is null`() = runTest {
        manager.onSearchQueryChanged(query = "query")
        val publicationFlow = MutableStateFlow<Publication?>(value = null)
        val results =
            manager.getSearchResults(publicationFlow = publicationFlow, scope = backgroundScope)

        val pagingData = results.first()
        val items = collectItems(pagingData = pagingData)
        assertTrue(items.isEmpty())
    }

    @Test
    fun `getSearchResults returns paging data with results`() = runTest {
        manager.onSearchQueryChanged(query = "query")
        val publication = mockk<Publication>()
        val publicationFlow = MutableStateFlow<Publication?>(value = publication)

        val locator1 = Locator(
            href = Url(url = "chapter1.html")!!,
            mediaType = MediaType(string = "text/html")!!,
            title = "Chapter 1",
        )
        val locator2 = Locator(
            href = Url(url = "chapter2.html")!!,
            mediaType = MediaType(string = "text/html")!!,
            title = "Chapter 2",
        )

        val collection = LocatorCollection(
            locators = listOf(locator1, locator2),
        )

        val iterator = mockk<SearchIterator>()
        coEvery { iterator.next() } returns Try.success(success = collection) andThen Try.success(
            success = null,
        )

        coEvery {
            publication.search(query = "query")
        } returns iterator

        val job = Job()
        val scope = CoroutineScope(context = testDispatcher + job)
        val results = manager.getSearchResults(publicationFlow = publicationFlow, scope = scope)

        val pagingData = results.first()

        val owner = object : LifecycleOwner {
            val registry = LifecycleRegistry(provider = this)
            override val lifecycle = registry
        }
        owner.registry.currentState = Lifecycle.State.STARTED

        val differ = AsyncPagingDataDiffer(
            diffCallback = object : DiffUtil.ItemCallback<SearchItem>() {
                override fun areItemsTheSame(oldItem: SearchItem, newItem: SearchItem) =
                    oldItem == newItem

                override fun areContentsTheSame(oldItem: SearchItem, newItem: SearchItem) =
                    oldItem == newItem
            },
            updateCallback = object : ListUpdateCallback {
                override fun onInserted(position: Int, count: Int) {}
                override fun onRemoved(position: Int, count: Int) {}
                override fun onMoved(fromPosition: Int, toPosition: Int) {}
                override fun onChanged(position: Int, count: Int, payload: Any?) {}
            },
            workerDispatcher = testDispatcher,
        )
        differ.submitData(lifecycle = owner.lifecycle, pagingData = pagingData)
        val items = differ.snapshot().items

        assertEquals(4, items.size)
        assertTrue(items[0] is SearchItem.Header)
        assertEquals("Chapter 1", (items[0] as SearchItem.Header).title)
        assertTrue(items[1] is SearchItem.Result)
        assertEquals(locator1, (items[1] as SearchItem.Result).locator)

        assertTrue(items[2] is SearchItem.Header)
        assertEquals("Chapter 2", (items[2] as SearchItem.Header).title)
        assertTrue(items[3] is SearchItem.Result)
        assertEquals(locator2, (items[3] as SearchItem.Result).locator)

        owner.registry.currentState = Lifecycle.State.DESTROYED
        scope.cancel()
    }

    private suspend fun collectItems(pagingData: PagingData<SearchItem>): List<SearchItem> {
        val differ = AsyncPagingDataDiffer(
            diffCallback = object : DiffUtil.ItemCallback<SearchItem>() {
                override fun areItemsTheSame(oldItem: SearchItem, newItem: SearchItem) =
                    oldItem == newItem

                override fun areContentsTheSame(oldItem: SearchItem, newItem: SearchItem) =
                    oldItem == newItem
            },
            updateCallback = object : ListUpdateCallback {
                override fun onInserted(position: Int, count: Int) {}
                override fun onRemoved(position: Int, count: Int) {}
                override fun onMoved(fromPosition: Int, toPosition: Int) {}
                override fun onChanged(position: Int, count: Int, payload: Any?) {}
            },
            workerDispatcher = testDispatcher,
        )
        differ.submitData(pagingData = pagingData)
        return differ.snapshot().items
    }
}
