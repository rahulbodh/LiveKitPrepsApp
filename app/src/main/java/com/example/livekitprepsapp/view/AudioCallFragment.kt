package com.example.livekitprepsapp.view

import android.os.Bundle
import android.os.Handler
import android.os.Looper // Import for Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.livekitprepsapp.R
import com.example.livekitprepsapp.databinding.FragmentAudioCallBinding
import java.util.concurrent.TimeUnit // Import for TimeUnit

class AudioCallFragment : BaseCallFragment() {

    private lateinit var binding: FragmentAudioCallBinding
    // Initialize Handler with Looper.getMainLooper() to ensure it runs on the UI thread
    private val handler = Handler(Looper.getMainLooper())
    private var isCallTimerRunning = false
    private var callStartTimeMillis: Long = 0

    // Runnable to update the call timer display every second
    private val updateCallTimeRunnable = object : Runnable {
        override fun run() {
            if (isCallTimerRunning) {
                val currentTimeMillis = System.currentTimeMillis()
                val elapsedTimeMillis = currentTimeMillis - callStartTimeMillis

                // Calculate hours, minutes, and seconds using TimeUnit for clarity
                val hours = TimeUnit.MILLISECONDS.toHours(elapsedTimeMillis)
                val minutes = TimeUnit.MILLISECONDS.toMinutes(elapsedTimeMillis) % 60
                val seconds = TimeUnit.MILLISECONDS.toSeconds(elapsedTimeMillis) % 60

                // Format the time string (HH:MM:SS if hours > 0, otherwise MM:SS)
                binding.statusText.text = if (hours > 0) {
                    String.format("%02d:%02d:%02d", hours, minutes, seconds)
                } else {
                    String.format("%02d:%02d", minutes, seconds)
                }

                // Schedule the next update in 1 second (1000 milliseconds)
                handler.postDelayed(this, 1000)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View { // Changed return type to non-nullable View
        // Inflate the layout for this fragment using the provided inflater
        binding = FragmentAudioCallBinding.inflate(inflater, container, false)

        // Load the caller image using Glide, applying a circular crop
        Glide.with(this)
            .load("https://images.pexels.com/photos/1391498/pexels-photo-1391498.jpeg](https://images.pexels.com/photos/1391498/pexels-photo-1391498.jpeg") // Updated image URL
            .apply(RequestOptions().circleCrop())
            .into(binding.callerImage)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Start the call timer when the view is created and ready
        Handler().postDelayed(
            {
                startCallTimer()
            },
            3000
        )

        binding.rejectButton.setOnClickListener {
            stopCallTimer()
            requireActivity().finish()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Stop the call timer and remove any pending callbacks when the view is destroyed
        stopCallTimer()
    }

    /**
     * Starts the call timer.
     * This method initializes the start time and posts the runnable to the handler.
     */
    private fun startCallTimer() {
        if (!isCallTimerRunning) {
            isCallTimerRunning = true
            callStartTimeMillis = System.currentTimeMillis() // Record the start time
            handler.post(updateCallTimeRunnable) // Start posting the runnable
        }
    }

    /**
     * Stops the call timer.
     * This method sets the running flag to false and removes all pending callbacks.
     */
    private fun stopCallTimer() {
        if (isCallTimerRunning) {
            isCallTimerRunning = false
            handler.removeCallbacks(updateCallTimeRunnable) // Stop any pending updates
        }
    }
}
