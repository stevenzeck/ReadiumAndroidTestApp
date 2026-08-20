package com.example.readiumandroidtestapp.features.reader.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class BookPreferencesRepositoryTest {

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(context = testDispatcher + Job())

    private fun createRepository(file: File): BookPreferencesRepository {
        val dataStore: DataStore<Preferences> = PreferenceDataStoreFactory.create(
            scope = testScope,
            produceFile = { file },
        )
        return BookPreferencesRepository(dataStore = dataStore)
    }

    @Test
    fun `save and get preferences`() = testScope.runTest {
        val file = temporaryFolder.newFile("test_prefs.preferences_pb")
        val repository = createRepository(file = file)
        val bookId = 1L
        val preferencesJson = """{"fontFamily": "SANS_SERIF"}"""

        assertNull(repository.getPreferences(bookId = bookId))

        repository.savePreferences(bookId = bookId, preferencesJson = preferencesJson)

        assertEquals(preferencesJson, repository.getPreferences(bookId = bookId))
    }

    @Test
    fun `save and get tts preferences`() = testScope.runTest {
        val file = temporaryFolder.newFile("test_tts_prefs.preferences_pb")
        val repository = createRepository(file = file)
        val bookId = 2L
        val preferencesJson = """{"rate": 1.5}"""

        assertNull(repository.getTtsPreferences(bookId = bookId))

        repository.saveTtsPreferences(bookId = bookId, preferencesJson = preferencesJson)

        assertEquals(preferencesJson, repository.getTtsPreferences(bookId = bookId))
    }

    @Test
    fun `save and get audiobook preferences`() = testScope.runTest {
        val file = temporaryFolder.newFile("test_audio_prefs.preferences_pb")
        val repository = createRepository(file = file)
        val bookId = 3L
        val preferencesJson = """{"volume": 0.8}"""

        assertNull(repository.getAudiobookPreferences(bookId = bookId))

        repository.saveAudiobookPreferences(bookId = bookId, preferencesJson = preferencesJson)

        assertEquals(preferencesJson, repository.getAudiobookPreferences(bookId = bookId))
    }
}
