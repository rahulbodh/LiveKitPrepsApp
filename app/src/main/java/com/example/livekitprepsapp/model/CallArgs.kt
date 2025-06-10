package com.example.livekitprepsapp.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CallArgs(
    val callType: String, // "audio" or "video"
    val token: String,
    val room: String
) : Parcelable
