package com.example.livekitprepsapp.broadcast

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.livekitprepsapp.utils.ForegroundService
import com.example.livekitprepsapp.view.InCallActivity
import timber.log.Timber

class CallActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Timber.tag("CallActionReceiver").d("onReceive: ${intent.action}")
        when (intent.action) {
            ForegroundService.ACTION_ACCEPT_CALL -> {
                // Create and launch PendingIntent
                val activityIntent = Intent(context, InCallActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TOP or
                            Intent.FLAG_ACTIVITY_SINGLE_TOP
                    putExtra("launchedFromCall", true)
                }

                val pendingIntent = PendingIntent.getActivity(
                    context, 1001, activityIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                try {
                    pendingIntent.send()
                } catch (e: PendingIntent.CanceledException) {
                    e.printStackTrace()
                }
            }
            ForegroundService.ACTION_REJECT_CALL -> {
                val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                manager.cancel(ForegroundService.DEFAULT_NOTIFICATION_ID)
            }
        }
    }

}
