package com.example.readiumandroidtestapp.features.reader.data

import org.readium.r2.navigator.epub.EpubDefaults
import org.readium.r2.navigator.epub.EpubNavigatorFactory
import org.readium.r2.navigator.epub.EpubPreferences
import org.readium.r2.navigator.preferences.PreferencesEditor
import org.readium.r2.shared.publication.Publication
import javax.inject.Inject

class DefaultEpubNavigatorFactoryWrapper @Inject constructor() : EpubNavigatorFactoryWrapper {
    override fun createPreferencesEditor(
        publication: Publication,
        initialPreferences: EpubPreferences,
    ): PreferencesEditor<EpubPreferences> {
        val factory = EpubNavigatorFactory(
            publication = publication,
            configuration = EpubNavigatorFactory.Configuration(defaults = EpubDefaults()),
        )
        return factory.createPreferencesEditor(currentPreferences = initialPreferences)
    }
}
