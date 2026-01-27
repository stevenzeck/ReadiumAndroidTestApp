package com.example.readiumandroidtestapp.features.reader.data

interface BookPreferencesRepository {
    suspend fun getPreferences(bookId: Long): String?
    suspend fun savePreferences(bookId: Long, preferencesJson: String)
    suspend fun getTtsPreferences(bookId: Long): String?
    suspend fun saveTtsPreferences(bookId: Long, preferencesJson: String)
    suspend fun getAudiobookPreferences(bookId: Long): String?
    suspend fun saveAudiobookPreferences(bookId: Long, preferencesJson: String)
}
