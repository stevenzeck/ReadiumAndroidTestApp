package com.example.readiumandroidtestapp.features.reader.data

import androidx.paging.PagingSource
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.readium.r2.shared.publication.Locator
import org.readium.r2.shared.publication.LocatorCollection
import org.readium.r2.shared.publication.services.search.SearchError
import org.readium.r2.shared.publication.services.search.SearchIterator
import org.readium.r2.shared.util.Try

@OptIn(ExperimentalCoroutinesApi::class)
class SearchPagingSourceTest {

    private val iterator: SearchIterator = mockk()
    private var loadedLocators: List<Locator>? = null
    private val pagingSource = SearchPagingSource(
        iterator = iterator,
        onPageLoaded = { loadedLocators = it },
    )

    @Test
    fun `load returns Page on success`() = runTest {
        val locators = listOf(mockk<Locator>(relaxed = true))
        val collection = LocatorCollection(locators = locators)

        coEvery { iterator.next() } returns Try.success(success = collection)

        val result = pagingSource.load(
            params = PagingSource.LoadParams.Refresh(
                key = 0,
                loadSize = 10,
                placeholdersEnabled = false,
            ),
        )

        assertTrue(result is PagingSource.LoadResult.Page)
        val page = result as PagingSource.LoadResult.Page
        assertEquals(locators, page.data)
        assertEquals(null, page.prevKey)
        assertEquals(1, page.nextKey)

        assertEquals(locators, loadedLocators)
    }

    @Test
    fun `load returns empty Page on end of data`() = runTest {
        // Iterator returns null when finished
        coEvery { iterator.next() } returns Try.success(success = null)

        val result = pagingSource.load(
            params = PagingSource.LoadParams.Refresh(
                key = 0,
                loadSize = 10,
                placeholdersEnabled = false,
            ),
        )

        assertTrue(result is PagingSource.LoadResult.Page)
        val page = result as PagingSource.LoadResult.Page
        assertTrue(page.data.isEmpty())
        assertEquals(null, page.nextKey)
    }

    @Test
    fun `load returns Error on failure`() = runTest {
        val searchError = mockk<SearchError>(relaxed = true)

        coEvery { iterator.next() } returns Try.failure(failure = searchError)

        val result = pagingSource.load(
            params = PagingSource.LoadParams.Refresh(
                key = 0,
                loadSize = 10,
                placeholdersEnabled = false,
            ),
        )

        assertTrue(result is PagingSource.LoadResult.Error)
        val error = result as PagingSource.LoadResult.Error
        assertTrue(error.throwable.message?.contains(other = "Search failed") == true)
    }
}
