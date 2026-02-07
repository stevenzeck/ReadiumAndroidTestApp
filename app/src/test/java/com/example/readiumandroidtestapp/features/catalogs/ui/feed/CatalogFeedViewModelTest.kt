package com.example.readiumandroidtestapp.features.catalogs.ui.feed

import com.example.readiumandroidtestapp.R
import com.example.readiumandroidtestapp.core.data.database.FakeCatalogDao
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
import kotlinx.coroutines.flow.first
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
    private val catalogDao = FakeCatalogDao()
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
        val catalog =
            Catalog(id = 1, title = "Feed", href = "http://feed", type = Catalog.TYPE_OPDS_1)
        catalogDao.insertCatalog(catalog = catalog)

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
        assertEquals(listOf(catalog), (state as CatalogFeedUiState.Success).catalogs)

        collectJob.cancel()
    }

    @Test
    fun `addCatalog success inserts catalog`() = runTest(context = testDispatcher) {
        val title = "Test Feed"
        val url = "http://example.com/feed"
        val parseData = mockk<ParseData> {
            every { type } returns Catalog.TYPE_OPDS_1
            every { this@mockk.feed?.title } returns title
        }

        coEvery {
            opdsParser.parseUrlString(url = url)
        } returns Try.success(success = parseData)

        viewModel = CatalogFeedViewModel(
            catalogDao = catalogDao,
            userMessageManager = userMessageManager,
            opdsParser = opdsParser,
        )

        viewModel.addCatalog(title = title, url = url)
        advanceUntilIdle()

        coVerify { opdsParser.parseUrlString(url = url) }

        val savedCatalogs = catalogDao.getCatalogModels().first()
        assertEquals(1, savedCatalogs.size)
        val savedCatalog = savedCatalogs.first()

        assertEquals(title, savedCatalog.title)
        assertEquals(url, savedCatalog.href)
        assertEquals(Catalog.TYPE_OPDS_1, savedCatalog.type)
    }

    @Test
    fun `addCatalog failure emits error message`() = runTest(context = testDispatcher) {
        val title = "Test Feed"
        val url = "http://example.com/feed"

        coEvery { opdsParser.parseUrlString(url = url) } returns Try.failure(
            failure = Exception("Error"),
        )

        viewModel = CatalogFeedViewModel(
            catalogDao = catalogDao,
            userMessageManager = userMessageManager,
            opdsParser = opdsParser,
        )

        viewModel.addCatalog(title = title, url = url)
        advanceUntilIdle()

        val savedCatalogs = catalogDao.getCatalogModels().first()
        assertTrue("DAO should be empty on failure", savedCatalogs.isEmpty())

        coVerify { userMessageManager.emitMessage(messageId = R.string.error_invalid_opds_feed) }
    }

    @Test
    fun `editCatalog calls dao insert with new data`() = runTest(context = testDispatcher) {
        val catalog = Catalog(id = 123L, title = "Old Title", href = "old_href", type = 1)
        catalogDao.insertCatalog(catalog = catalog)

        val newTitle = "New Title"

        viewModel = CatalogFeedViewModel(
            catalogDao = catalogDao,
            userMessageManager = userMessageManager,
            opdsParser = opdsParser,
        )

        viewModel.editCatalog(catalog = catalog, newTitle = newTitle)
        advanceUntilIdle()

        val savedCatalogs = catalogDao.getCatalogModels().first()
        assertEquals(1, savedCatalogs.size)

        val updatedCatalog = savedCatalogs.first()
        assertEquals(newTitle, updatedCatalog.title)
        assertEquals(catalog.id, updatedCatalog.id)
    }
}
