package com.example.livekitprepsapp.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.livekitprepsapp.R
import com.example.livekitprepsapp.view.CallActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import timber.log.Timber

class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        const val CHANNEL_ID = "call_channel"
        const val NOTIFICATION_ID = 1001
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "Refreshed token: $token")
        // Send token to your backend if needed
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val data = remoteMessage.data
        Timber.tag("FCM").d("Received message: $data")
        Log.d("FCM", "Received message: $data")

        when (data["type"]) {
            "CALL_INVITE" -> {
                showIncomingCallNotification(data)
            }
            "CALL_ACCEPTED" -> {
                // Handle accepted (optional)
            }
            "CALL_REJECTED" -> {
                // Handle rejected (optional)
            }
        }

        remoteMessage.notification?.body?.let {
            Timber.tag("FCM").d("Notification Body: $it")
            Log.d("FCM" , "Nofication Body: $it")
            sendNotification(it)
        }
    }

    private fun showIncomingCallNotification(data: Map<String, String>) {
        val intent = Intent(this, CallActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("callerName", data["callerName"])
            putExtra("callerId", data["callerId"])
            putExtra("channel", data["channel"])
            putExtra("callType", data["callType"])
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create channel if needed
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Incoming Calls",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for incoming call notifications"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.banner_dark) // Replace with your app icon
            .setContentTitle("Incoming Call")
            .setContentText("Call from ${data["callerName"] ?: "Unknown"}")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setAutoCancel(true)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setFullScreenIntent(pendingIntent, true)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun sendNotification(messageBody: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Default Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("FCM Notification")
            .setContentText(messageBody)
            .setSmallIcon(R.drawable.banner_dark)
            .setAutoCancel(true)

        notificationManager.notify(0, notificationBuilder.build())
    }
}
