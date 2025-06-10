package com.example.livekitprepsapp.broadcast

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.livekitprepsapp.utils.ForegroundService
import com.example.livekitprepsapp.view.InCallActivity
import io.livekit.android.LiveKit
import io.livekit.android.room.Room
import timber.log.Timber // Assuming Timber is used for logging, if not, remove this import
import kotlin.jvm.java

/**
 * BroadcastReceiver to handle call actions from foreground service notifications.
 * This receiver is responsible for launching the call UI, rejecting incoming calls,
 * and ending active calls, including disconnecting from the LiveKit room and stopping the service.
 */
class CallActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        Log.d("CallActionReceiver", "Received action: ${intent.action}")

        when (intent.action) {
            ForegroundService.ACTION_ACCEPT_CALL -> {
                Log.d("CallActionReceiver", "Action: ACCEPT_CALL - Launching InCallActivity.")
                // Cancel the notification immediately since the call is being accepted and the UI will take over.
                notificationManager.cancel(ForegroundService.DEFAULT_NOTIFICATION_ID)

                // Create an Intent to launch InCallActivity.
                // FLAG_ACTIVITY_NEW_TASK: Ensures the activity starts in a new task if necessary.
                // FLAG_ACTIVITY_CLEAR_TOP: If the activity is already running, all activities on top of it are cleared.
                // FLAG_ACTIVITY_SINGLE_TOP: If the activity is already at the top of the stack, it's not relaunched.
                val activityIntent = Intent(context, InCallActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TOP or
                            Intent.FLAG_ACTIVITY_SINGLE_TOP
                    // Pass a flag to indicate that the activity was launched from a call notification.
                    putExtra("launchedFromCall", true)
                }

                // Create a PendingIntent to launch the activity.
                // FLAG_UPDATE_CURRENT: If the PendingIntent already exists, update its extra data.
                // FLAG_IMMUTABLE: Makes the PendingIntent immutable for security reasons (required on API 23+).
                val pendingIntent = PendingIntent.getActivity(
                    context, 1001, activityIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                try {
                    // Send the PendingIntent to launch the InCallActivity.
                    pendingIntent.send()
                } catch (e: PendingIntent.CanceledException) {
                    Log.e("CallActionReceiver", "Failed to send PendingIntent for ACCEPT_CALL: ${e.message}")
                    e.printStackTrace()
                }
            }
            ForegroundService.ACTION_REJECT_CALL -> {
                Log.d("CallActionReceiver", "Action: REJECT_CALL - Disconnecting and stopping service.")
                // Cancel the call notification.
                notificationManager.cancel(ForegroundService.DEFAULT_NOTIFICATION_ID)

                // Attempt to get the active LiveKit Room instance using the application context.
                // It's crucial to use applicationContext here as BroadcastReceiver's context might be short-lived.
//                LiveKit.getRoom(context.applicationContext)?.let { room ->
//                    Log.d("CallActionReceiver", "LiveKit room found. Disconnecting on reject.")
//                    room.disconnect() // Disconnect from the LiveKit room.
//                } ?: run {
//                    Log.d("CallActionReceiver", "No active LiveKit room found to disconnect on reject.")
//                }

                // Stop the ForegroundService, as the call has been rejected.
                val serviceIntent = Intent(context, ForegroundService::class.java)
                context.stopService(serviceIntent)
            }
            ForegroundService.ACTION_HANG_UP -> {
                Log.d("CallActionReceiver", "Action: END_CALL - Disconnecting and stopping service.")
                // Cancel the call notification.
                notificationManager.cancel(ForegroundService.DEFAULT_NOTIFICATION_ID)

                // Attempt to get the active LiveKit Room instance using the application context.
//                LiveKit.getRoom(context.applicationContext)?.let { room ->
//                    Log.d("CallActionReceiver", "LiveKit room found. Disconnecting on end call.")
//                    room.disconnect() // Disconnect from the LiveKit room.
//                } ?: run {
//                    Log.d("CallActionReceiver", "No active LiveKit room found to disconnect on end call.")
//                }

                // Stop the ForegroundService, as the call has ended.
                val serviceIntent = Intent(context, ForegroundService::class.java)
                context.stopService(serviceIntent)
            }
            // If you had a separate ACTION_HANG_UP previously, it's now covered by ACTION_END_CALL.
            // If you need a distinct action for hanging up, you would add another 'when' case here.
        }
    }
}
