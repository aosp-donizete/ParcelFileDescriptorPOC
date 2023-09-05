package com.android.sharefiles

import android.app.Service
import android.content.Intent
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.android.sharefiles.MainFileProvider.Companion.realImagesPath
import java.io.File

class MainService : Service() {

    private val notificationManager by lazy {
        NotificationManagerCompat.from(this)
    }

    override fun onCreate() {
        super.onCreate()
        NotificationChannelCompat.Builder(
            "my_channel",
            NotificationManagerCompat.IMPORTANCE_DEFAULT
        ).apply {
            setName("my_channel")
        }.build().run(notificationManager::createNotificationChannel)

        NotificationCompat.Builder(
            this,
            "my_channel"
        ).apply {
            setSmallIcon(R.drawable.ic_launcher_foreground)
        }.build().also {
            startForeground(1234, it)
        }

        Log.d(TAG, "MainService is running")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?) = object : IShareFiles.Stub() {
        override fun getAvailableFiles(): List<String> {
            return realImagesPath.listFiles()?.mapNotNull {
                it.takeIf { it.isFile }?.toString()
            } ?: emptyList()
        }

        override fun getFileDescriptor(file: String): ParcelFileDescriptor {
            val mode = ParcelFileDescriptor.MODE_READ_ONLY
            return ParcelFileDescriptor.open(File(file), mode)
        }
    }

    companion object {
        const val TAG = "MainService"
    }
}