package com.example.readiumandroidtestapp.features.reader.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import org.readium.r2.shared.publication.Locator
import org.readium.r2.shared.publication.services.search.SearchIterator
import org.readium.r2.shared.util.Try

class SearchPagingSource(
    private val iterator: SearchIterator,
    private val onPageLoaded: (List<Locator>) -> Unit,
) : PagingSource<Int, Locator>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Locator> {
        val pageNumber = params.key ?: 0

        return try {
            val collection = when (val result = iterator.next()) {
                is Try.Success -> result.value
                is Try.Failure -> throw Exception("Search failed: ${result.value}")
            }

            if (collection == null) {
                LoadResult.Page(
                    data = emptyList(),
                    prevKey = null,
                    nextKey = null,
                )
            } else {
                onPageLoaded(collection.locators)

                LoadResult.Page(
                    data = collection.locators,
                    prevKey = if (pageNumber > 0) pageNumber - 1 else null,
                    nextKey = if (collection.locators.isNotEmpty()) pageNumber + 1 else null,
                )
            }
        } catch (e: Exception) {
            LoadResult.Error(throwable = e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Locator>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }
}
