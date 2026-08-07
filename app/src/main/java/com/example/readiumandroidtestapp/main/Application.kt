package com.example.readiumandroidtestapp.main

import android.app.Application
import androidx.media3.cast.Cast
import androidx.media3.cast.CastParams
import com.google.android.gms.cast.CastMediaControlIntent
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

        try {
            val castParams = CastParams.Builder()
                .setReceiverApplicationId(CastMediaControlIntent.DEFAULT_MEDIA_RECEIVER_APPLICATION_ID)
                .build()
            Cast.getSingletonInstance(this).initialize(castParams)
        } catch (e: Exception) {
            Timber.e(e, "Failed to initialize Media3 Cast")
        }
    }
}
