package com.example.readiumandroidtestapp.main

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

/**
 * The standard Android Application class.
 *
 * Annotated with @HiltAndroidApp to trigger Hilt's code generation,
 * serving as the application-level dependency container.
 */
@HiltAndroidApp
class Application : Application() {

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
    }
}
