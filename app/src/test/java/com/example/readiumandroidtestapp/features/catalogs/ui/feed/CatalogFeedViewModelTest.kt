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
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.readium.r2.shared.opds.ParseData
import org.readium.r2.shared.util.Try

@OptIn(ExperimentalCoroutinesApi::class)
class CatalogFeedViewModelTest {

    private val catalogDao: CatalogDao = mockk()
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
    fun `uiState emits Loading initially`() = runTest {
        every { catalogDao.getCatalogModels() } returns flowOf(value = emptyList())
        val viewModel = CatalogFeedViewModel(
            catalogDao = catalogDao,
            userMessageManager = userMessageManager,
            opdsParser = opdsParser,
        )

        assertEquals(CatalogFeedUiState.Loading, viewModel.catalogsState.value)
    }

    @Test
    fun `uiState emits Success when catalogs exist`() = runTest {
        val catalogs = listOf(
            Catalog(
                id = 1,
                title = "Test Catalog",
                href = "http://test.com/opds",
                type = 1,
            ),
        )
        every { catalogDao.getCatalogModels() } returns flowOf(value = catalogs)
        val viewModel = CatalogFeedViewModel(
            catalogDao = catalogDao,
            userMessageManager = userMessageManager,
            opdsParser = opdsParser,
        )

        backgroundScope.launch(context = UnconfinedTestDispatcher(scheduler = testScheduler)) {
            viewModel.catalogsState.collect()
        }

        advanceUntilIdle()

        val state = viewModel.catalogsState.value
        assertTrue(state is CatalogFeedUiState.Success)
        assertEquals(catalogs, (state as CatalogFeedUiState.Success).catalogs)
    }

    @Test
    fun `uiState emits Error on exception`() = runTest {
        every { catalogDao.getCatalogModels() } returns flow { throw RuntimeException("Error") }
        val viewModel = CatalogFeedViewModel(
            catalogDao = catalogDao,
            userMessageManager = userMessageManager,
            opdsParser = opdsParser,
        )

        backgroundScope.launch(context = UnconfinedTestDispatcher(scheduler = testScheduler)) {
            viewModel.catalogsState.collect()
        }

        advanceUntilIdle()

        assertEquals(CatalogFeedUiState.Error, viewModel.catalogsState.value)
    }

    @Test
    fun `addCatalog inserts catalog on valid OPDS feed`() = runTest {
        val title = "New Catalog"
        val url = "http://example.com/feed"
        val parseData = mockk<ParseData>()
        every { parseData.type } returns 2

        every { catalogDao.getCatalogModels() } returns flowOf(value = emptyList())
        coEvery { opdsParser.parseUrlString(url = url) } returns Try.success(success = parseData)
        coEvery { catalogDao.insertCatalog(catalog = any()) } returns 1L

        val viewModel = CatalogFeedViewModel(
            catalogDao = catalogDao,
            userMessageManager = userMessageManager,
            opdsParser = opdsParser,
        )

        viewModel.addCatalog(title = title, url = url)

        advanceUntilIdle()

        coVerify {
            catalogDao.insertCatalog(
                catalog = Catalog(
                    title = title,
                    href = url,
                    type = 2,
                ),
            )
        }
    }

    @Test
    fun `addCatalog emits error message on invalid OPDS feed`() = runTest {
        val title = "Invalid Catalog"
        val url = "http://example.com/invalid"

        every { catalogDao.getCatalogModels() } returns flowOf(value = emptyList())
        coEvery { opdsParser.parseUrlString(url = url) } returns Try.failure(failure = Exception("Invalid"))

        val viewModel = CatalogFeedViewModel(
            catalogDao = catalogDao,
            userMessageManager = userMessageManager,
            opdsParser = opdsParser,
        )

        viewModel.addCatalog(title = title, url = url)

        advanceUntilIdle()

        coVerify { userMessageManager.emitMessage(messageId = R.string.error_invalid_opds_feed) }
        coVerify(exactly = 0) { catalogDao.insertCatalog(catalog = any()) }
    }

    @Test
    fun `deleteCatalog calls dao delete`() = runTest {
        val catalog = Catalog(id = 123, title = "Delete Me", href = "url", type = 1)
        every { catalogDao.getCatalogModels() } returns flowOf(value = emptyList())
        coEvery { catalogDao.deleteCatalog(id = 123) } returns Unit

        val viewModel = CatalogFeedViewModel(
            catalogDao = catalogDao,
            userMessageManager = userMessageManager,
            opdsParser = opdsParser,
        )

        viewModel.deleteCatalog(catalog = catalog)

        advanceUntilIdle()

        coVerify { catalogDao.deleteCatalog(id = 123) }
    }

    @Test
    fun `editCatalog calls dao insert with new title`() = runTest {
        val catalog = Catalog(id = 1, title = "Old Title", href = "url", type = 1)
        val newTitle = "New Title"
        every { catalogDao.getCatalogModels() } returns flowOf(value = emptyList())
        coEvery { catalogDao.insertCatalog(catalog = any()) } returns 1L

        val viewModel = CatalogFeedViewModel(
            catalogDao = catalogDao,
            userMessageManager = userMessageManager,
            opdsParser = opdsParser,
        )

        viewModel.editCatalog(catalog = catalog, newTitle = newTitle)

        advanceUntilIdle()

        coVerify {
            catalogDao.insertCatalog(
                catalog = catalog.copy(title = newTitle),
            )
        }
    }
}
