package com.example.readiumandroidtestapp.features.reader.ui.search

import androidx.paging.AsyncPagingDataDiffer
import androidx.paging.PagingData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListUpdateCallback
import com.example.readiumandroidtestapp.features.reader.domain.SearchGateway
import com.example.readiumandroidtestapp.features.reader.ui.state.SearchItem
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
import org.readium.r2.shared.publication.Publication
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class DefaultReaderSearchManagerTest {

    private val searchGateway: SearchGateway = mockk()
    private val manager = DefaultReaderSearchManager(searchGateway = searchGateway)
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher = testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `onSearchQueryChanged updates searchQuery`() = runTest(context = testDispatcher) {
        manager.onSearchQueryChanged(query = "test")
        assertEquals("test", manager.searchQuery.value)
    }

    @Test
    fun `getSearchResults returns empty when query is null`() = runTest(context = testDispatcher) {
        val publicationFlow = MutableStateFlow<Publication?>(value = mockk())
        val results =
            manager.getSearchResults(publicationFlow = publicationFlow, scope = backgroundScope)

        val pagingData = results.first()
        val items = collectItems(pagingData = pagingData)
        assertTrue(items.isEmpty())
    }

    @Test
    fun `getSearchResults returns empty when publication is null`() =
        runTest(context = testDispatcher) {
            manager.onSearchQueryChanged(query = "query")
            val publicationFlow = MutableStateFlow<Publication?>(value = null)
            val results =
                manager.getSearchResults(publicationFlow = publicationFlow, scope = backgroundScope)

            val pagingData = results.first()
            val items = collectItems(pagingData = pagingData)
            assertTrue(items.isEmpty())
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
