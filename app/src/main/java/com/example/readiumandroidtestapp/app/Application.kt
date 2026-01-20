package com.example.readiumandroidtestapp.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * The standard Android Application class.
 *
 * Annotated with @HiltAndroidApp to trigger Hilt's code generation,
 * serving as the application-level dependency container.
 */
@HiltAndroidApp
class Application : Application()
