package com.example.livekitprepsapp.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat

class ForegroundService : Service() {

    companion object {
        const val DEFAULT_NOTIFICATION_ID = 3456
        const val DEFAULT_CHANNEL_ID = "livekit_example_foreground"
    }


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }

        val actualNotification =
            NotificationCompat.Builder(this, DEFAULT_CHANNEL_ID)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build()

        startForeground(DEFAULT_NOTIFICATION_ID, actualNotification)

        return START_NOT_STICKY

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            DEFAULT_CHANNEL_ID,
            "Foreground",
            NotificationManager.IMPORTANCE_LOW
        )
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }


}