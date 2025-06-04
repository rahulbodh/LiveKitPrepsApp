package com.example.livekitprepsapp.utils

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.livekitprepsapp.R

class ForegroundService : Service() {

    companion object {
        const val DEFAULT_NOTIFICATION_ID = 3456
        const val DEFAULT_CHANNEL_ID = "livekit_example_foreground"
        const val ACTION_ACCEPT_CALL = "com.example.livekitprepsapp.ACCEPT_CALL"
        const val ACTION_REJECT_CALL = "com.example.livekitprepsapp.REJECT_CALL"
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val callerName = intent?.getStringExtra("callerName") ?: "Unknown"
        val isIncomingCall = intent?.getBooleanExtra("isIncomingCall", false) ?: false

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }

        val builder = NotificationCompat.Builder(this, DEFAULT_CHANNEL_ID)
            .setSmallIcon(R.drawable.banner_dark)
            .setContentTitle("LiveKit Call")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setOngoing(true)
            .setAutoCancel(false)

        if (isIncomingCall) {
            builder.setContentText("Incoming call from $callerName")

            // Accept Button Intent
            val acceptIntent = Intent(this, CallActionReceiver::class.java).apply {
                action = ACTION_ACCEPT_CALL
            }
            val acceptPendingIntent = PendingIntent.getBroadcast(
                this, 0, acceptIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Reject Button Intent
            val rejectIntent = Intent(this, CallActionReceiver::class.java).apply {
                action = ACTION_REJECT_CALL
            }
            val rejectPendingIntent = PendingIntent.getBroadcast(
                this, 1, rejectIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            builder.addAction(R.drawable.ic_call_white_24dp, "Accept", acceptPendingIntent)
            builder.addAction(R.drawable.ic_call_end_white_24dp, "Reject", rejectPendingIntent)

            // Optional: Full-screen intent for lock screen
            val fullScreenIntent = Intent(this, IncomingCallActivity::class.java)
            val fullScreenPendingIntent = PendingIntent.getActivity(
                this, 2, fullScreenIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.setFullScreenIntent(fullScreenPendingIntent, true)

        } else {
            builder.setContentText("You're in a call")
        }

        val notification = builder.build()
        startForeground(DEFAULT_NOTIFICATION_ID, notification)

        return START_NOT_STICKY
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            DEFAULT_CHANNEL_ID,
            "Call Notifications",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Used for call notifications"
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}
