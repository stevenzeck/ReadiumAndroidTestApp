package com.example.readiumandroidtestapp.features.reader.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookPreferencesRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {

    private fun preferencesKey(bookId: Long) = stringPreferencesKey(name = "preferences_$bookId")
    private fun ttsPreferencesKey(bookId: Long) =
        stringPreferencesKey(name = "tts_preferences_$bookId")

    private fun audiobookPreferencesKey(bookId: Long) =
        stringPreferencesKey(name = "audiobook_preferences_$bookId")

    suspend fun getPreferences(bookId: Long): String? {
        val key = preferencesKey(bookId = bookId)
        return dataStore.data.map { preferences -> preferences[key] }
            .firstOrNull()
    }

    suspend fun savePreferences(bookId: Long, preferencesJson: String) {
        val key = preferencesKey(bookId = bookId)
        dataStore.edit { preferences ->
            preferences[key] = preferencesJson
        }
    }

    suspend fun getTtsPreferences(bookId: Long): String? {
        val key = ttsPreferencesKey(bookId = bookId)
        return dataStore.data.map { preferences -> preferences[key] }
            .firstOrNull()
    }

    suspend fun saveTtsPreferences(bookId: Long, preferencesJson: String) {
        val key = ttsPreferencesKey(bookId = bookId)
        dataStore.edit { preferences ->
            preferences[key] = preferencesJson
        }
    }

    suspend fun getAudiobookPreferences(bookId: Long): String? {
        val key = audiobookPreferencesKey(bookId = bookId)
        return dataStore.data.map { preferences -> preferences[key] }
            .firstOrNull()
    }

    suspend fun saveAudiobookPreferences(bookId: Long, preferencesJson: String) {
        val key = audiobookPreferencesKey(bookId = bookId)
        dataStore.edit { preferences ->
            preferences[key] = preferencesJson
        }
    }
}
