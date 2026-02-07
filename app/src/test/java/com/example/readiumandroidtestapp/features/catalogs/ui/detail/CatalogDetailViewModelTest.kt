package com.example.readiumandroidtestapp.features.catalogs.ui.detail

import com.example.readiumandroidtestapp.core.domain.model.Catalog
import com.example.readiumandroidtestapp.core.domain.opds.OpdsParser
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
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
import org.readium.r2.shared.opds.Feed
import org.readium.r2.shared.opds.ParseData
import org.readium.r2.shared.util.Try
import org.readium.r2.shared.util.Url
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class CatalogDetailViewModelTest {

    private lateinit var viewModel: CatalogDetailViewModel
    private val opdsParser: OpdsParser = mockk()
    private val testDispatcher = StandardTestDispatcher()

    private val catalog = Catalog(
        id = 1,
        title = "Test",
        href = "http://test.com",
        type = 1,
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher = testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init fetches feed successfully`() = runTest(context = testDispatcher) {
        val feed = Feed(title = "My Feed", type = 1, href = Url(url = "http://test.com")!!)
        val parseData = ParseData(feed = feed, publication = null, type = 1)

        coEvery {
            opdsParser.parseUrlString(url = catalog.href, type = catalog.type)
        } returns Try.success(success = parseData)

        viewModel = CatalogDetailViewModel(
            catalog = catalog,
            opdsParser = opdsParser,
        )

        val collectJob = backgroundScope.launch(context = UnconfinedTestDispatcher(testScheduler)) {
            viewModel.feedState.collect()
        }
        advanceUntilIdle()

        val state = viewModel.feedState.value
        assertTrue(state is FeedState.Success)
        assertEquals(feed, (state as FeedState.Success).feed)

        collectJob.cancel()
    }

    @Test
    fun `init handles fetch failure`() = runTest(context = testDispatcher) {
        val errorMessage = "Network error"
        coEvery {
            opdsParser.parseUrlString(url = catalog.href, type = catalog.type)
        } returns Try.failure(failure = Exception(errorMessage))

        viewModel = CatalogDetailViewModel(
            catalog = catalog,
            opdsParser = opdsParser,
        )

        val collectJob =
            backgroundScope.launch(context = UnconfinedTestDispatcher(scheduler = testScheduler)) {
                viewModel.feedState.collect()
            }
        advanceUntilIdle()

        val state = viewModel.feedState.value
        assertTrue(state is FeedState.Error)
        assertEquals(errorMessage, (state as FeedState.Error).message)

        collectJob.cancel()
    }
}
