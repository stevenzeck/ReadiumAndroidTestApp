package com.example.readiumandroidtestapp.features.reader.data

import kotlinx.serialization.json.Json
import org.readium.navigator.web.reflowable.preferences.ReflowableWebPreferences

class ReflowableWebPreferencesSerializer(
    private val json: Json = Json { ignoreUnknownKeys = true },
) {
    fun serialize(preferences: ReflowableWebPreferences): String =
        json.encodeToString(serializer = ReflowableWebPreferences.serializer(), value = preferences)

    fun deserialize(preferences: String): ReflowableWebPreferences = json.decodeFromString(
        deserializer = ReflowableWebPreferences.serializer(),
        string = preferences,
    )
}
