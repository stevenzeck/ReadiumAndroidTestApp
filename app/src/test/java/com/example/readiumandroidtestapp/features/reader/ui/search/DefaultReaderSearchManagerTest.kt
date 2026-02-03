package com.example.readiumandroidtestapp.features.reader.ui.search

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.readiumandroidtestapp.features.reader.domain.SearchGateway
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalCoroutinesApi::class)
class DefaultReaderSearchManagerTest {

    private val searchGateway: SearchGateway = mockk()
    private val dispatcher = StandardTestDispatcher()
    private lateinit var manager: DefaultReaderSearchManager

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher = dispatcher)
        manager = DefaultReaderSearchManager(searchGateway = searchGateway)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `onSearchQueryChanged updates query`() {
        manager.onSearchQueryChanged(query = "test")
        assertEquals("test", manager.searchQuery.value)
    }

    // TODO: Enable this test once androidx.paging:paging-testing is added.
    @Ignore("Requires paging-testing artifact to test PagingData flow collection properly")
    @Test
    fun `getSearchResults triggers search and updates decorations`() {

    }
}
