package com.example.readiumandroidtestapp.features.catalogs.ui.feed

import com.example.readiumandroidtestapp.R
import com.example.readiumandroidtestapp.core.data.database.CatalogDao
import com.example.readiumandroidtestapp.core.domain.model.Catalog
import com.example.readiumandroidtestapp.core.domain.opds.OpdsParser
import com.example.readiumandroidtestapp.core.utils.UserMessageManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
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
import org.readium.r2.shared.opds.ParseData
import org.readium.r2.shared.util.Try

@OptIn(ExperimentalCoroutinesApi::class)
class CatalogFeedViewModelTest {

    private lateinit var viewModel: CatalogFeedViewModel
    private val catalogDao: CatalogDao = mockk(relaxed = true)
    private val userMessageManager: UserMessageManager = mockk(relaxed = true)
    private val opdsParser: OpdsParser = mockk()
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher = testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `catalogsState emits Success when catalogs exist`() = runTest(context = testDispatcher) {
        val catalogs = listOf(mockk<Catalog>())
        every { catalogDao.getCatalogModels() } returns MutableStateFlow(value = catalogs)

        viewModel = CatalogFeedViewModel(
            catalogDao = catalogDao,
            userMessageManager = userMessageManager,
            opdsParser = opdsParser,
        )

        val collectJob = backgroundScope.launch(context = UnconfinedTestDispatcher(testScheduler)) {
            viewModel.catalogsState.collect()
        }

        advanceUntilIdle()

        val state = viewModel.catalogsState.value
        assertTrue(state is CatalogFeedUiState.Success)
        assertEquals(catalogs, (state as CatalogFeedUiState.Success).catalogs)

        collectJob.cancel()
    }

    @Test
    fun `addCatalog success inserts catalog`() = runTest(context = testDispatcher) {
        val title = "Test Feed"
        val url = "http://example.com/feed"
        val parseData = mockk<ParseData> {
            every { type } returns Catalog.TYPE_OPDS_1
        }

        // Setup initial flow to avoid crash/hang if init runs
        every { catalogDao.getCatalogModels() } returns MutableStateFlow(value = emptyList())

        coEvery {
            opdsParser.parseUrlString(
                url = url,
                type = any(),
            )
        } returns Try.success(success = parseData)

        viewModel = CatalogFeedViewModel(
            catalogDao = catalogDao,
            userMessageManager = userMessageManager,
            opdsParser = opdsParser,
        )

        viewModel.addCatalog(title = title, url = url)
        advanceUntilIdle()

        coVerify { opdsParser.parseUrlString(url = url) }
        coVerify {
            catalogDao.insertCatalog(
                catalog = withArg { catalog ->
                    assertEquals(title, catalog.title)
                    assertEquals(url, catalog.href)
                    assertEquals(Catalog.TYPE_OPDS_1, catalog.type)
                },
            )
        }
    }

    @Test
    fun `addCatalog failure emits error message`() = runTest(context = testDispatcher) {
        val title = "Test Feed"
        val url = "http://example.com/feed"

        every { catalogDao.getCatalogModels() } returns MutableStateFlow(value = emptyList())
        coEvery { opdsParser.parseUrlString(url = url, type = any()) } returns Try.failure(
            failure = Exception("Error"),
        )

        viewModel = CatalogFeedViewModel(
            catalogDao = catalogDao,
            userMessageManager = userMessageManager,
            opdsParser = opdsParser,
        )

        viewModel.addCatalog(title = title, url = url)
        advanceUntilIdle()

        coVerify(exactly = 0) { catalogDao.insertCatalog(catalog = any()) }
        coVerify { userMessageManager.emitMessage(messageId = R.string.error_invalid_opds_feed) }
    }

    @Test
    fun `editCatalog calls dao insert with new title`() = runTest(context = testDispatcher) {
        val catalog = Catalog(id = 123L, title = "Old Title", href = "href", type = 1)
        val newTitle = "New Title"
        every { catalogDao.getCatalogModels() } returns MutableStateFlow(value = emptyList())

        viewModel = CatalogFeedViewModel(
            catalogDao = catalogDao,
            userMessageManager = userMessageManager,
            opdsParser = opdsParser,
        )

        viewModel.editCatalog(catalog, newTitle)
        advanceUntilIdle()

        coVerify {
            catalogDao.insertCatalog(
                catalog = withArg { updatedCatalog ->
                    assertEquals(newTitle, updatedCatalog.title)
                    assertEquals(catalog.id, updatedCatalog.id)
                    assertEquals(catalog.href, updatedCatalog.href)
                },
            )
        }
    }
}
