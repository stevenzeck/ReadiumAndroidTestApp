package com.example.readiumandroidtestapp.features.catalogs.ui.detail

import com.example.readiumandroidtestapp.core.domain.model.Catalog
import com.example.readiumandroidtestapp.core.domain.opds.OpdsParser
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.readium.r2.shared.opds.Feed
import org.readium.r2.shared.opds.ParseData
import org.readium.r2.shared.util.Try

@OptIn(ExperimentalCoroutinesApi::class)
class CatalogDetailViewModelTest {

    private val opdsParser: OpdsParser = mockk()
    private val testDispatcher = StandardTestDispatcher()

    private val catalog = Catalog(
        id = 1L,
        title = "Test Catalog",
        href = "http://example.com/feed",
        type = Catalog.TYPE_OPDS_1,
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Loading`() = runTest {
        // Mock parser to return a dummy value (or just hang if we don't advance)
        // Since init block calls fetchFeed, we need to mock it to avoid crash
        coEvery {
            opdsParser.parseUrlString(
                url = any(),
                type = any(),
            )
        } returns Try.success(success = mockk(relaxed = true))

        val viewModel = CatalogDetailViewModel(
            catalog = catalog,
            opdsParser = opdsParser,
        )

        assertEquals(FeedState.Loading, viewModel.feedState.value)
    }

    @Test
    fun `fetchFeed calls parser with correct arguments`() = runTest {
        coEvery {
            opdsParser.parseUrlString(
                url = any(),
                type = any(),
            )
        } returns Try.success(success = mockk(relaxed = true))

        CatalogDetailViewModel(
            catalog = catalog,
            opdsParser = opdsParser,
        )

        advanceUntilIdle()

        coVerify { opdsParser.parseUrlString(url = catalog.href, type = catalog.type) }
    }

    @Test
    fun `feedState is Success when parser succeeds`() = runTest {
        val feed: Feed = mockk()
        val parseData: ParseData = mockk(relaxed = true)
        coEvery { parseData.feed } returns feed
        coEvery {
            opdsParser.parseUrlString(
                url = any(),
                type = any(),
            )
        } returns Try.success(success = parseData)

        val viewModel = CatalogDetailViewModel(
            catalog = catalog,
            opdsParser = opdsParser,
        )

        advanceUntilIdle()

        assertTrue(viewModel.feedState.value is FeedState.Success)
        assertEquals(feed, (viewModel.feedState.value as FeedState.Success).feed)
    }

    @Test
    fun `feedState is Error when parser fails`() = runTest {
        val errorMessage = "Network Error"
        coEvery {
            opdsParser.parseUrlString(
                url = any(),
                type = any(),
            )
        } returns Try.failure(failure = Exception(errorMessage))

        val viewModel = CatalogDetailViewModel(
            catalog = catalog,
            opdsParser = opdsParser,
        )

        advanceUntilIdle()

        assertTrue(viewModel.feedState.value is FeedState.Error)
        assertEquals(errorMessage, (viewModel.feedState.value as FeedState.Error).message)
    }
}
