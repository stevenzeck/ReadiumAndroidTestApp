package com.example.readiumandroidtestapp.features.reader.data

import org.junit.Assert.assertNotNull
import org.junit.Test

class DefaultPreferencesSerializerFactoryTest {

    private val factory = DefaultPreferencesSerializerFactory()

    @Test
    fun createEpubSerializer_returnsInstance() {
        assertNotNull(factory.createEpubSerializer())
    }

    @Test
    fun createPdfiumSerializer_returnsInstance() {
        assertNotNull(factory.createPdfiumSerializer())
    }

    @Test
    fun createAndroidTtsSerializer_returnsInstance() {
        assertNotNull(factory.createAndroidTtsSerializer())
    }

    @Test
    fun createExoPlayerSerializer_returnsInstance() {
        assertNotNull(factory.createExoPlayerSerializer())
    }
}
