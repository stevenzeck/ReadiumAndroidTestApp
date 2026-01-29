package com.example.readiumandroidtestapp.features.reader.ui.tts

import com.example.readiumandroidtestapp.features.reader.domain.TtsNavigatorGateway
import com.example.readiumandroidtestapp.features.reader.domain.TtsServiceGateway
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.readium.r2.navigator.VisualNavigator
import org.readium.r2.shared.publication.Locator
import org.readium.r2.shared.publication.Publication

@OptIn(ExperimentalCoroutinesApi::class)
class ReaderTtsManagerTest {

    private val ttsServiceGateway: TtsServiceGateway = mockk()
    private val testDispatcher = UnconfinedTestDispatcher()
    private val visualNavigator: VisualNavigator = mockk(relaxed = true)

    private lateinit var manager: ReaderTtsManager

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher = testDispatcher)
        manager = ReaderTtsManager(ttsServiceGateway = ttsServiceGateway)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `start creates navigator and starts playback`() = runTest(context = testDispatcher) {
        val publication = mockk<Publication>(relaxed = true)
        val initialLocator = mockk<Locator>(relaxed = true)
        val navigatorGateway = mockk<TtsNavigatorGateway>(relaxed = true)

        coEvery { visualNavigator.firstVisibleElementLocator() } returns initialLocator

        coEvery {
            ttsServiceGateway.createNavigator(
                publication = publication,
                initialLocator = initialLocator,
                listener = any(),
            )
        } returns Result.success(value = navigatorGateway)

        every { navigatorGateway.currentLocator } returns MutableStateFlow(value = initialLocator)

        manager.initFactory(publication = publication)
        manager.start(
            visualNavigator = visualNavigator,
            scope = backgroundScope,
            onStop = {},
        )

        advanceUntilIdle()

        verify { navigatorGateway.play() }
        assertTrue(manager.isTtsActive.value)
    }

    @Test
    fun `start fails gracefully if navigator creation fails`() = runTest(context = testDispatcher) {
        val publication = mockk<Publication>(relaxed = true)
        val initialLocator = mockk<Locator>(relaxed = true)

        coEvery { visualNavigator.firstVisibleElementLocator() } returns initialLocator

        coEvery {
            ttsServiceGateway.createNavigator(
                publication = publication,
                initialLocator = initialLocator,
                listener = any(),
            )
        } returns Result.failure(Exception("Creation failed"))

        manager.initFactory(publication = publication)
        manager.start(
            visualNavigator = visualNavigator,
            scope = backgroundScope,
            onStop = {},
        )

        advanceUntilIdle()

        assertFalse(manager.isTtsActive.value)
    }

    @Test
    fun `stop closes navigator and resets state`() = runTest(context = testDispatcher) {
        val publication = mockk<Publication>(relaxed = true)
        val initialLocator = mockk<Locator>(relaxed = true)
        val navigatorGateway = mockk<TtsNavigatorGateway>(relaxed = true)

        coEvery { visualNavigator.firstVisibleElementLocator() } returns initialLocator
        coEvery {
            ttsServiceGateway.createNavigator(
                publication = publication,
                initialLocator = initialLocator,
                listener = any(),
            )
        } returns Result.success(value = navigatorGateway)
        every { navigatorGateway.currentLocator } returns MutableStateFlow(value = initialLocator)

        manager.initFactory(publication = publication)
        manager.start(
            visualNavigator = visualNavigator,
            scope = backgroundScope,
            onStop = {},
        )
        advanceUntilIdle()

        manager.stop(visualNavigator = visualNavigator, scope = backgroundScope)
        advanceUntilIdle()

        verify { navigatorGateway.close() }
        assertFalse(manager.isTtsActive.value)
    }

    @Test
    fun `play delegates to navigator`() = runTest(context = testDispatcher) {
        val publication = mockk<Publication>(relaxed = true)
        val initialLocator = mockk<Locator>(relaxed = true)
        val navigatorGateway = mockk<TtsNavigatorGateway>(relaxed = true)

        coEvery { visualNavigator.firstVisibleElementLocator() } returns initialLocator
        coEvery {
            ttsServiceGateway.createNavigator(
                publication = publication,
                initialLocator = initialLocator,
                listener = any(),
            )
        } returns Result.success(value = navigatorGateway)
        every { navigatorGateway.currentLocator } returns MutableStateFlow(value = initialLocator)

        manager.initFactory(publication = publication)
        manager.start(
            visualNavigator = visualNavigator,
            scope = backgroundScope,
            onStop = {},
        )
        advanceUntilIdle()

        manager.play()
        verify { navigatorGateway.play() }
    }
}
