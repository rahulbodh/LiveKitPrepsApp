package com.example.livekitprepsapp.utils

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Color
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.livekitprepsapp.R
import com.example.livekitprepsapp.broadcast.CallActionReceiver
import com.example.livekitprepsapp.view.InCallActivity

class ForegroundService : Service() {

    companion object {
        const val DEFAULT_NOTIFICATION_ID = 3456
        const val DEFAULT_CHANNEL_ID = "livekit_example_foreground"
        const val ACTION_ACCEPT_CALL = "com.example.livekitprepsapp.ACCEPT_CALL"
        const val ACTION_REJECT_CALL = "com.example.livekitprepsapp.REJECT_CALL"
        const val ACTION_HANG_UP = "com.example.livekitperpsapp.HANG_UP"
    }

    override fun onBind(intent: Intent?): IBinder? = null

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val callerName = intent?.getStringExtra("callerName") ?: "Unknown"
        val isIncomingCall = intent?.getBooleanExtra("isIncomingCall", false) ?: false

        Log.d("ForegroundService", "onStartCommand: $callerName")

        createNotificationChannel()

        val person = Person.Builder()
            .setName(callerName)
            .setImportant(true)
            .build()

        // Answer PendingIntent
        val answerIntent = Intent(this, CallActionReceiver::class.java).apply {
            action = ACTION_ACCEPT_CALL
        }
        val answerPendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            answerIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Decline PendingIntent
        val declineIntent = Intent(this, CallActionReceiver::class.java).apply {
            action = ACTION_REJECT_CALL
        }
        val declinePendingIntent = PendingIntent.getBroadcast(
            this,
            1,
            declineIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val hangUpIntent = Intent(this , CallActionReceiver::class.java).apply {
            action = ACTION_HANG_UP
        }

        val hangUpPendingIntent = PendingIntent.getBroadcast(
            this,
            2,
            hangUpIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        // Full-screen intent for incoming call
        val fullScreenIntent = Intent(this, InCallActivity::class.java)
            .setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP
            )
            .putExtra("launchedFromCall", true)
        val fullScreenPendingIntent = PendingIntent.getActivity(
            this,
            2,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Use Notification.Builder (not NotificationCompat)
        val builder = Notification.Builder(this, DEFAULT_CHANNEL_ID)
            .setSmallIcon(R.drawable.banner_dark)
            .setContentTitle("LiveKit Call")
            .setContentText(if (isIncomingCall) "Incoming call from $callerName" else "You're in a call")
            .setCategory(Notification.CATEGORY_CALL)
            .setOngoing(true)
            .setAutoCancel(false)
            .setPriority(Notification.PRIORITY_HIGH)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setStyle(
                Notification.CallStyle.forIncomingCall(
                    person,
                    declinePendingIntent,
                    answerPendingIntent
                )
            )
            .setStyle(
                Notification.CallStyle.forOngoingCall(
                    person,
                    hangUpPendingIntent
                )
            )

            val notification = builder.build()
            notification.flags = notification.flags or Notification.FLAG_ONGOING_EVENT
            startForeground(DEFAULT_NOTIFICATION_ID, notification  , ServiceInfo.FOREGROUND_SERVICE_TYPE_PHONE_CALL)
            Log.d("ForegroundService", "Notification shoot")



        return START_NOT_STICKY
    }



    fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                DEFAULT_CHANNEL_ID,
                "LiveKit Calls",
                NotificationManager.IMPORTANCE_HIGH // Must be HIGH for heads-up
            ).apply {
                description = "Channel for incoming LiveKit calls"
                enableLights(true)
                lightColor = Color.RED
                enableVibration(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

}
