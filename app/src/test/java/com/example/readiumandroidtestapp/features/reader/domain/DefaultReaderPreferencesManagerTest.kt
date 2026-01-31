package com.example.readiumandroidtestapp.features.reader.domain

import android.app.Application
import com.example.readiumandroidtestapp.features.reader.data.AndroidTtsNavigatorFactoryWrapper
import com.example.readiumandroidtestapp.features.reader.data.BookPreferencesRepository
import com.example.readiumandroidtestapp.features.reader.ui.audio.AppAudioNavigatorFactory
import com.example.readiumandroidtestapp.features.reader.ui.tts.ReaderTtsManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.readium.adapter.exoplayer.audio.ExoPlayerPreferences
import org.readium.navigator.media.audio.AudioNavigator
import org.readium.navigator.media.tts.AndroidTtsNavigatorFactory
import org.readium.navigator.media.tts.android.AndroidTtsEngine
import org.readium.navigator.media.tts.android.AndroidTtsPreferences
import org.readium.navigator.media.tts.android.AndroidTtsPreferencesEditor
import org.readium.r2.navigator.VisualNavigator
import org.readium.r2.navigator.epub.EpubPreferences
import org.readium.r2.navigator.epub.EpubSettings
import org.readium.r2.navigator.preferences.Configurable
import org.readium.r2.navigator.preferences.Preference
import org.readium.r2.shared.publication.Publication
import org.readium.r2.shared.util.Language

class DefaultReaderPreferencesManagerTest {

    private val bookPreferencesRepository: BookPreferencesRepository = mockk(relaxed = true)
    private val audioNavigatorFactory: AppAudioNavigatorFactory = mockk(relaxed = true)
    private val ttsNavigatorFactoryWrapper: AndroidTtsNavigatorFactoryWrapper =
        mockk(relaxed = true)
    private val application: Application = mockk()
    private val ttsManager: ReaderTtsManager = mockk(relaxed = true)

    private val manager = DefaultReaderPreferencesManager(
        bookPreferencesRepository = bookPreferencesRepository,
        audioNavigatorFactory = audioNavigatorFactory,
        ttsNavigatorFactoryWrapper = ttsNavigatorFactoryWrapper,
    )

    interface ConfigurableVisualNavigator : VisualNavigator,
        Configurable<EpubSettings, EpubPreferences>

    @Test
    fun `commitPreferences saves epub preferences`() = runTest {
        val bookId = 1L
        val preferences = EpubPreferences()
        val visualNavigator = mockk<ConfigurableVisualNavigator>(relaxed = true)

        manager.commitPreferences(
            bookId = bookId,
            preferences = preferences,
            currentVisualNavigator = visualNavigator,
            audioNavigator = null,
            ttsManager = ttsManager,
        )

        verify { visualNavigator.submitPreferences(preferences = preferences) }
        coVerify {
            bookPreferencesRepository.savePreferences(
                bookId = bookId,
                preferencesJson = any(),
            )
        }
    }

    @Test
    fun `commitPreferences saves tts preferences`() = runTest {
        val bookId = 1L
        val preferences = AndroidTtsPreferences()

        manager.commitPreferences(
            bookId = bookId,
            preferences = preferences,
            currentVisualNavigator = null,
            audioNavigator = null,
            ttsManager = ttsManager,
        )

        verify { ttsManager.submitPreferences(preferences = preferences) }
        coVerify {
            bookPreferencesRepository.saveTtsPreferences(
                bookId = bookId,
                preferencesJson = any(),
            )
        }
    }

    @Test
    fun `commitPreferences saves audio preferences`() = runTest {
        val bookId = 1L
        val preferences = ExoPlayerPreferences()
        val audioNavigator = mockk<AudioNavigator<*, ExoPlayerPreferences>>(relaxed = true)

        manager.commitPreferences(
            bookId = bookId,
            preferences = preferences,
            currentVisualNavigator = null,
            audioNavigator = audioNavigator,
            ttsManager = ttsManager,
        )

        verify { audioNavigator.submitPreferences(preferences = preferences) }
        coVerify {
            bookPreferencesRepository.saveAudiobookPreferences(
                bookId = bookId,
                preferencesJson = any(),
            )
        }
    }

    @Test
    fun `createTtsSettingsSession returns session on success`() = runTest {
        val bookId = 1L
        val publication = mockk<Publication>(relaxed = true)
        val factory = mockk<AndroidTtsNavigatorFactory>(relaxed = true)
        val editor = mockk<AndroidTtsPreferencesEditor>(relaxed = true)

        coEvery {
            ttsNavigatorFactoryWrapper.createFactory(
                application = application,
                publication = publication,
            )
        } returns factory
        coEvery { bookPreferencesRepository.getTtsPreferences(bookId = bookId) } returns null
        every { factory.createPreferencesEditor(preferences = any()) } returns editor

        // Mock editor properties explicitly
        val languagePref = mockk<Preference<Language?>>(relaxed = true)
        every { languagePref.value } returns null
        every { editor.language } returns languagePref

        val voicesPref = mockk<Preference<Map<Language, AndroidTtsEngine.Voice.Id>>>(relaxed = true)
        every { voicesPref.value } returns emptyMap()
        every { editor.voices } returns voicesPref

        every { ttsManager.voices } returns emptySet()

        val session = manager.createTtsSettingsSession(
            bookId = bookId,
            publication = publication,
            ttsManager = ttsManager,
            application = application,
        )

        assertNotNull(session)
        coVerify {
            ttsNavigatorFactoryWrapper.createFactory(
                application = application,
                publication = publication,
            )
        }
    }
}
