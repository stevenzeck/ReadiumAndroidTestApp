package com.example.readiumandroidtestapp.features.reader.ui.audio

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.compose.foundation.ExperimentalFoundationApi
import com.example.readiumandroidtestapp.main.MainActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import org.readium.navigator.media.audio.AudioNavigator
import timber.log.Timber
import javax.inject.Inject

class DefaultReaderMediaBinder @Inject constructor(
    @ApplicationContext private val context: Context,
) : ReaderMediaBinder {
    private var serviceConnection: ServiceConnection? = null
    private var isServiceBound = false

    @OptIn(ExperimentalFoundationApi::class)
    override fun bind(
        navigator: AudioNavigator<*, *>,
    ) {
        if (isServiceBound) return

        val connection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                val binder = service as? MediaService.LocalBinder
                if (binder == null) {
                    Timber.e(message = "Service is bound, but returned IBinder is not LocalBinder. Check Intent action.")
                }

                val activityIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                }

                binder?.openSession(navigator = navigator, activityIntent = activityIntent)
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                // Service crashed or killed
            }
        }

        val intent = Intent(context, MediaService::class.java)

        try {
            context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
            serviceConnection = connection
            isServiceBound = true
        } catch (e: Exception) {
            Timber.e(t = e, message = "Failed to bind to MediaService")
        }
    }

    override fun unbind() {
        serviceConnection?.let {
            if (isServiceBound) {
                context.unbindService(it)
                isServiceBound = false
            }
        }
        serviceConnection = null
    }
}
