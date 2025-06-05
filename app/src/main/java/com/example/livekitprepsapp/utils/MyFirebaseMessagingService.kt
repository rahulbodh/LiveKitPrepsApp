package com.example.livekitprepsapp.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Person
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.livekitprepsapp.R
import com.example.livekitprepsapp.broadcast.CallActionReceiver
import com.example.livekitprepsapp.utils.ForegroundService.Companion.DEFAULT_CHANNEL_ID
import com.example.livekitprepsapp.view.CallActivity
import com.example.livekitprepsapp.view.InCallActivity
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

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val data = remoteMessage.data
        Timber.tag("FCM").d("Received message: $data")
        Log.d("FCM", "Received message: $data")

        val intent = Intent(this, ForegroundService::class.java).apply {
            putExtra("callerName", data["callerName"])
            putExtra("callerId", data["callerId"])
            putExtra("channel", data["channel"])
            putExtra("callType", data["callType"])
            putExtra("isIncomingCall", true)
            action = "ACTION_START_CALL_SERVICE"
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }

        when (data["type"]) {
            "CALL_INVITE" -> {



            }
        }
    }

}
