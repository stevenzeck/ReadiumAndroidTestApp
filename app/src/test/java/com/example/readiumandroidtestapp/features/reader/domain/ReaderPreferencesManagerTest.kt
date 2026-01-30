package com.example.readiumandroidtestapp.features.reader.domain

import com.example.readiumandroidtestapp.features.reader.data.BookPreferencesRepository
import com.example.readiumandroidtestapp.features.reader.ui.audio.AppAudioNavigatorFactory
import com.example.readiumandroidtestapp.features.reader.ui.tts.ReaderTtsManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.readium.adapter.exoplayer.audio.ExoPlayerPreferences

class ReaderPreferencesManagerTest {

    private val bookPreferencesRepository: BookPreferencesRepository = mockk(relaxed = true)
    private val audioNavigatorFactory: AppAudioNavigatorFactory = mockk()
    private val ttsManager: ReaderTtsManager = mockk(relaxed = true)

    private val readerPreferencesManager = DefaultReaderPreferencesManager(
        bookPreferencesRepository = bookPreferencesRepository,
        audioNavigatorFactory = audioNavigatorFactory,
    )

    @Test
    fun `loadAudiobookPreferences returns preferences from repository`() = runTest {
        val bookId = 123L
        val json = "{}"
        coEvery { bookPreferencesRepository.getAudiobookPreferences(bookId = bookId) } returns json

        readerPreferencesManager.loadAudiobookPreferences(bookId = bookId)

        coVerify { bookPreferencesRepository.getAudiobookPreferences(bookId = bookId) }
    }

    @Test
    fun `commitPreferences saves ExoPlayerPreferences`() = runTest {
        val bookId = 123L
        val preferences = ExoPlayerPreferences()

        readerPreferencesManager.commitPreferences(
            bookId = bookId,
            preferences = preferences,
            currentVisualNavigator = null,
            audioNavigator = mockk(relaxed = true),
            ttsManager = ttsManager,
        )

        coVerify {
            bookPreferencesRepository.saveAudiobookPreferences(
                bookId = bookId,
                preferencesJson = any(),
            )
        }
    }
}
