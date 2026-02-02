package com.example.readiumandroidtestapp.features.reader.data

import org.readium.r2.navigator.epub.EpubPreferences
import org.readium.r2.navigator.preferences.PreferencesEditor
import org.readium.r2.shared.publication.Publication

interface EpubNavigatorFactoryWrapper {
    fun createPreferencesEditor(
        publication: Publication,
        initialPreferences: EpubPreferences,
    ): PreferencesEditor<EpubPreferences>
}
