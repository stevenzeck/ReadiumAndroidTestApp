package com.example.readiumandroidtestapp.features.reader.ui.audio

import org.readium.navigator.media.audio.AudioNavigator

interface ReaderMediaBinder {
    fun bind(navigator: AudioNavigator<*, *>)
    fun unbind()
}
