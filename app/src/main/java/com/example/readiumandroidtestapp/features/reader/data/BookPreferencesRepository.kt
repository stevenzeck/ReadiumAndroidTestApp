package com.example.readiumandroidtestapp.features.reader.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.bookPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(name = "book_preferences")

@Singleton
class BookPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    private fun preferencesKey(bookId: Long) = stringPreferencesKey("preferences_$bookId")
    private fun ttsPreferencesKey(bookId: Long) = stringPreferencesKey("tts_preferences_$bookId")
    private fun audiobookPreferencesKey(bookId: Long) =
        stringPreferencesKey("audiobook_preferences_$bookId")

    suspend fun getPreferences(bookId: Long): String? {
        val key = preferencesKey(bookId)
        return context.bookPreferencesDataStore.data.map { preferences -> preferences[key] }
            .firstOrNull()
    }

    suspend fun savePreferences(bookId: Long, preferencesJson: String) {
        val key = preferencesKey(bookId)
        context.bookPreferencesDataStore.edit { preferences ->
            preferences[key] = preferencesJson
        }
    }

    suspend fun getTtsPreferences(bookId: Long): String? {
        val key = ttsPreferencesKey(bookId)
        return context.bookPreferencesDataStore.data.map { preferences -> preferences[key] }
            .firstOrNull()
    }

    suspend fun saveTtsPreferences(bookId: Long, preferencesJson: String) {
        val key = ttsPreferencesKey(bookId)
        context.bookPreferencesDataStore.edit { preferences ->
            preferences[key] = preferencesJson
        }
    }

    suspend fun getAudiobookPreferences(bookId: Long): String? {
        val key = audiobookPreferencesKey(bookId)
        return context.bookPreferencesDataStore.data.map { preferences -> preferences[key] }
            .firstOrNull()
    }

    suspend fun saveAudiobookPreferences(bookId: Long, preferencesJson: String) {
        val key = audiobookPreferencesKey(bookId)
        context.bookPreferencesDataStore.edit { preferences ->
            preferences[key] = preferencesJson
        }
    }
}
