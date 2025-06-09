package com.example.livekitprepsapp.utils

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.legacy.content.WakefulBroadcastReceiver

class BackgroundFCMReceiver : WakefulBroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val extras = intent?.extras?.keySet()
        if (extras != null) {
            for (key in extras) {
                Log.d("FCMRCVR", "$key")
            }
        }
    }
}