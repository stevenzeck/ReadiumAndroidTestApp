package com.example.readiumandroidtestapp.features.reader.data

import kotlinx.serialization.json.Json
import org.readium.navigator.web.fixedlayout.preferences.FixedWebPreferences

class FixedWebPreferencesSerializer(
    private val json: Json = Json { ignoreUnknownKeys = true },
) {
    fun serialize(preferences: FixedWebPreferences): String =
        json.encodeToString(serializer = FixedWebPreferences.serializer(), value = preferences)

    fun deserialize(preferences: String): FixedWebPreferences = json.decodeFromString(
        deserializer = FixedWebPreferences.serializer(),
        string = preferences,
    )
}
