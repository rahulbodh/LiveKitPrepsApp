package com.example.livekitprepsapp.utils

import android.content.Intent
import android.os.Build
import android.util.Log // Ensure this is the correct import for Logcat
import androidx.annotation.RequiresApi
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
// import timber.log.Timber // Commented out if not fully configured for Logcat

class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        const val CHANNEL_ID = "call_channel"
        const val NOTIFICATION_ID = 1001
        private const val TAG = "MyFCMService" // Consistent tag for easy filtering
    }

    /**
     * Called if FCM registration token is updated.
     * This might happen when the token expires, or the device is restored.
     * You should send the new token to your app server.
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Refreshed token: $token")
        // TODO: Send this token to your backend server so it can send messages to this specific device.
        // For example: yourBackendApi.sendRegistrationToken(token)
    }

    /**
     * Called when a message is received.
     * This method is triggered for data-only messages regardless of the app's state
     * (foreground, background, or killed), provided the message has 'high' priority.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Log.d(TAG, "---------------------------------------------")
        Log.d(TAG, "Message received from: ${remoteMessage.from}")
        Log.d(TAG, "Message ID: ${remoteMessage.messageId}")
        Log.d(TAG, "Collapse Key: ${remoteMessage.collapseKey}")
        Log.d(TAG, "TTL: ${remoteMessage.ttl} seconds")


        // Check if the message contains a data payload (which it should for data-only messages)
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "FCM Data Payload Received (Raw): ${remoteMessage.data}")

            // Log each key-value pair in the data payload for detailed inspection
            Log.d(TAG, "Parsing FCM Data Payload:")
            remoteMessage.data.forEach { (key, value) ->
                Log.d(TAG, "  Key: '$key', Value: '$value'")
            }

            // Extract specific data points
            val callerName = remoteMessage.data["name"] ?: "Unknown Caller"
            val callerId = remoteMessage.data["userId"] ?: "unknown_id" // Assuming userId from payload
            val channel = remoteMessage.data["channel"] ?: "default_channel"
            val callType = remoteMessage.data["type"] ?: "audio" // Assuming 'type' is for callType

            Log.d(TAG, "Extracted Data: CallerName='$callerName', CallerId='$callerId', Channel='$channel', CallType='$callType'")

            // Your existing logic to start ForegroundService based on message type
            when (callType) { // Changed from data["type"] to callType for consistency
                "CALL_INVITE" -> {
                    val intent = Intent(this, ForegroundService::class.java).apply {
                        putExtra("callerName", callerName)
                        putExtra("callerId", callerId)
                        putExtra("channel", channel)
                        putExtra("callType", callType)
                        putExtra("isIncomingCall", true)
                        action = "ACTION_START_CALL_SERVICE"
                    }

                    // Start the service based on Android version
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        startForegroundService(intent)
                        Log.d(TAG, "Starting ForegroundService for CALL_INVITE (API 26+)")
                    } else {
                        startService(intent)
                        Log.d(TAG, "Starting Service for CALL_INVITE (API < 26)")
                    }
                }
                // Handle other message types if necessary
                else -> {
                    Log.d(TAG, "Unhandled message type: $callType")
                }
            }
        } else {
            Log.d(TAG, "FCM message received but data payload is empty.")
        }

        // It's good practice to log if a notification payload is present (shouldn't be for data-only)
        remoteMessage.notification?.let {
            Log.w(TAG, "Unexpected Notification Payload received: Title=${it.title}, Body=${it.body}")
            // If you get this, it means your server is NOT sending a pure data-only message.
            // When app is in background/killed, this notification would be displayed by the system.
        }
        Log.d(TAG, "---------------------------------------------")
    }

    // You might also need an `onDeletedMessages()` override if your app handles message deletions
    // override fun onDeletedMessages() {
    //    super.onDeletedMessages()
    //    Log.d(TAG, "Messages deleted on server without being delivered.")
    //    // Implement logic to sync data with your server, as some messages might have been missed.
    // }
}
