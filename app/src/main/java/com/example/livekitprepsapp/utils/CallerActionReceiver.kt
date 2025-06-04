package com.example.livekitprepsapp.utils

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlin.jvm.java


class CallActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent.action) {
            ForegroundService.ACTION_ACCEPT_CALL -> {
                // Start call activity
                val callIntent = Intent(context, IncallActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(callIntent)
            }
            ForegroundService.ACTION_REJECT_CALL -> {
                // Stop service or do any cleanup
                val notificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.cancel(ForegroundService.DEFAULT_NOTIFICATION_ID)
            }
        }
    }
}
