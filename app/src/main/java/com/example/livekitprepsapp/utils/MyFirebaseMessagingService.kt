package com.example.livekitprepsapp.utils

import android.content.Intent
import com.example.livekitprepsapp.view.CallActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlin.jvm.java

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val data = remoteMessage.data

        when (data["type"]) {
            "CALL_INVITE" -> {
                // Show full-screen incoming call UI
                showIncomingCallNotification(data)
            }
            "CALL_ACCEPTED" -> {
                // Handle if needed
            }
            "CALL_REJECTED" -> {
                // Dismiss call or update UI
            }
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
        startActivity(intent)
    }
}
