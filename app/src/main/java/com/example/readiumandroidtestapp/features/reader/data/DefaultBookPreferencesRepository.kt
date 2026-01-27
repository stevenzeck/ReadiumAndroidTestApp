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
class DefaultBookPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) : BookPreferencesRepository {

    private fun preferencesKey(bookId: Long) = stringPreferencesKey(name = "preferences_$bookId")
    private fun ttsPreferencesKey(bookId: Long) =
        stringPreferencesKey(name = "tts_preferences_$bookId")

    private fun audiobookPreferencesKey(bookId: Long) =
        stringPreferencesKey(name = "audiobook_preferences_$bookId")

    override suspend fun getPreferences(bookId: Long): String? {
        val key = preferencesKey(bookId = bookId)
        return context.bookPreferencesDataStore.data.map { preferences -> preferences[key] }
            .firstOrNull()
    }

    override suspend fun savePreferences(bookId: Long, preferencesJson: String) {
        val key = preferencesKey(bookId = bookId)
        context.bookPreferencesDataStore.edit { preferences ->
            preferences[key] = preferencesJson
        }
    }

    override suspend fun getTtsPreferences(bookId: Long): String? {
        val key = ttsPreferencesKey(bookId = bookId)
        return context.bookPreferencesDataStore.data.map { preferences -> preferences[key] }
            .firstOrNull()
    }

    override suspend fun saveTtsPreferences(bookId: Long, preferencesJson: String) {
        val key = ttsPreferencesKey(bookId = bookId)
        context.bookPreferencesDataStore.edit { preferences ->
            preferences[key] = preferencesJson
        }
    }

    override suspend fun getAudiobookPreferences(bookId: Long): String? {
        val key = audiobookPreferencesKey(bookId = bookId)
        return context.bookPreferencesDataStore.data.map { preferences -> preferences[key] }
            .firstOrNull()
    }

    override suspend fun saveAudiobookPreferences(bookId: Long, preferencesJson: String) {
        val key = audiobookPreferencesKey(bookId = bookId)
        context.bookPreferencesDataStore.edit { preferences ->
            preferences[key] = preferencesJson
        }
    }
}
