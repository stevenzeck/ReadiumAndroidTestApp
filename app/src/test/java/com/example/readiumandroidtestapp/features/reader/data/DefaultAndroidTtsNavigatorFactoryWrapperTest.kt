package com.example.readiumandroidtestapp.features.reader.data

import android.app.Application
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.readium.navigator.media.tts.AndroidTtsNavigator
import org.readium.navigator.media.tts.AndroidTtsNavigatorFactory
import org.readium.navigator.media.tts.TtsNavigator
import org.readium.navigator.media.tts.TtsNavigatorFactory
import org.readium.r2.shared.publication.Locator
import org.readium.r2.shared.publication.Publication
import org.readium.r2.shared.util.Try

class DefaultAndroidTtsNavigatorFactoryWrapperTest {

    private val provider: AndroidTtsNavigatorFactoryProvider = mockk()
    private val wrapper = DefaultAndroidTtsNavigatorFactoryWrapper(factoryProvider = provider)
    private val application: Application = mockk()
    private val publication: Publication = mockk()
    private val factory: AndroidTtsNavigatorFactory = mockk()
    private val navigator: AndroidTtsNavigator = mockk()
    private val listener: TtsNavigator.Listener = mockk()
    private val locator: Locator = mockk()

    @Test
    fun `createNavigator returns success when factory and navigator are created`() = runTest {
        coEvery {
            provider.create(
                application = application,
                publication = publication,
            )
        } returns factory
        coEvery {
            factory.createNavigator(initialLocator = locator, listener = listener)
        } returns Try.success(navigator)

        val result = wrapper.createNavigator(
            application = application,
            publication = publication,
            initialLocator = locator,
            listener = listener,
        )

        assertTrue(result.isSuccess)
        assertEquals(navigator, result.getOrNull())
    }

    @Test
    fun `createNavigator returns failure when factory creation returns null`() = runTest {
        coEvery {
            provider.create(
                application = application,
                publication = publication,
            )
        } returns null

        val result = wrapper.createNavigator(
            application = application,
            publication = publication,
            initialLocator = locator,
            listener = listener,
        )

        assertTrue(result.isFailure)
        assertEquals("Failed to create TTS Factory", result.exceptionOrNull()?.message)
    }

    @Test
    fun `createNavigator returns failure when navigator creation fails`() = runTest {
        coEvery {
            provider.create(
                application = application,
                publication = publication,
            )
        } returns factory

        val error = mockk<TtsNavigatorFactory.Error>()
        coEvery {
            factory.createNavigator(initialLocator = locator, listener = listener)
        } returns Try.failure(error)

        val result = wrapper.createNavigator(
            application = application,
            publication = publication,
            initialLocator = locator,
            listener = listener,
        )

        assertTrue(result.isFailure)
        assertEquals("TTS creation failed: $error", result.exceptionOrNull()?.message)
    }

    @Test
    fun `createFactory returns factory created by provider`() = runTest {
        coEvery {
            provider.create(
                application = application,
                publication = publication,
            )
        } returns factory

        val result = wrapper.createFactory(application = application, publication = publication)

        assertEquals(factory, result)
    }
}
