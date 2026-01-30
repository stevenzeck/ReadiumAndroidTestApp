package com.example.readiumandroidtestapp.features.reader.data

import android.app.Application
import com.example.readiumandroidtestapp.features.reader.domain.TtsNavigatorGateway
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.readium.navigator.media.tts.AndroidTtsNavigator
import org.readium.navigator.media.tts.TtsNavigator
import org.readium.r2.shared.publication.Locator
import org.readium.r2.shared.publication.Publication

class DefaultTtsServiceGatewayTest {

    private val application: Application = mockk()
    private val factoryWrapper: AndroidTtsNavigatorFactoryWrapper = mockk()
    private lateinit var gateway: DefaultTtsServiceGateway

    @Before
    fun setUp() {
        gateway = DefaultTtsServiceGateway(
            application = application,
            factoryWrapper = factoryWrapper,
        )
    }

    @Test
    fun `createNavigator returns success when factory succeeds`() = runTest {
        val publication = mockk<Publication>()
        val initialLocator = mockk<Locator>()
        val listener = mockk<TtsNavigatorGateway.Listener>()
        val androidNavigator = mockk<AndroidTtsNavigator>(relaxed = true)

        coEvery {
            factoryWrapper.createNavigator(
                application = application,
                publication = publication,
                initialLocator = initialLocator,
                listener = any(),
            )
        } returns Result.success(value = androidNavigator)

        val result = gateway.createNavigator(
            publication = publication,
            initialLocator = initialLocator,
            listener = listener,
        )

        assertTrue(result.isSuccess)
    }

    @Test
    fun `createNavigator returns failure when factory fails`() = runTest {
        val publication = mockk<Publication>()
        val initialLocator = mockk<Locator>()
        val listener = mockk<TtsNavigatorGateway.Listener>()

        coEvery {
            factoryWrapper.createNavigator(
                application = application,
                publication = publication,
                initialLocator = initialLocator,
                listener = any(),
            )
        } returns Result.failure(Exception("Creation failed"))

        val result = gateway.createNavigator(
            publication = publication,
            initialLocator = initialLocator,
            listener = listener,
        )

        assertTrue(result.isFailure)
    }

    @Test
    fun `createNavigator passes listener correctly`() = runTest {
        val publication = mockk<Publication>()
        val initialLocator = mockk<Locator>()
        val listener = mockk<TtsNavigatorGateway.Listener>(relaxed = true)
        val androidNavigator = mockk<AndroidTtsNavigator>(relaxed = true)
        val ttsListenerSlot = slot<TtsNavigator.Listener>()

        coEvery {
            factoryWrapper.createNavigator(
                application = application,
                publication = publication,
                initialLocator = initialLocator,
                listener = capture(lst = ttsListenerSlot),
            )
        } returns Result.success(value = androidNavigator)

        gateway.createNavigator(
            publication = publication,
            initialLocator = initialLocator,
            listener = listener,
        )

        // Trigger the listener passed to factory wrapper
        ttsListenerSlot.captured.onStopRequested()

        // Verify original listener is called
        verify { listener.onStopRequested() }
    }
}
