package com.example.livekitprepsapp.view

import android.app.Activity
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Parcelable
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.livekitprepsapp.R
import com.example.livekitprepsapp.databinding.ActivityAudioCallBinding
import com.example.livekitprepsapp.databinding.ActivityCallBinding
import com.example.livekitprepsapp.dialog.showSelectAudioDeviceDialog
import com.example.livekitprepsapp.model.ParticipantItem
import com.example.livekitprepsapp.model.StressTest
import com.example.livekitprepsapp.viewModels.CallViewModel
import com.example.livekitprepsapp.viewModels.viewModelByFactory
import com.xwray.groupie.GroupieAdapter
import io.livekit.android.room.participant.LocalParticipant
import io.livekit.android.room.participant.Participant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import java.util.concurrent.TimeUnit

class AudioCallActivity : AppCompatActivity() {

    private val viewModel: CallViewModel by viewModelByFactory {
        val args = intent.getParcelableExtra<BundleArgs>(KEY_ARGS)
            ?: throw NullPointerException("args is null!")
        CallViewModel(
            url = args.url,
            token = args.token,
            e2ee = args.e2eeOn,
            e2eeKey = args.e2eeKey,
            stressTest = args.stressTest,
            application = application,
            videoCall = false
        )
    }

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

    private lateinit var binding: ActivityAudioCallBinding
    private val screenCaptureIntentLauncher =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
        ) { result ->
            val resultCode = result.resultCode
            val data = result.data
            if (resultCode != Activity.RESULT_OK || data == null) {
                return@registerForActivityResult
            }
            viewModel.startScreenCapture(data)
        }

    @androidx.camera.camera2.interop.ExperimentalCamera2Interop
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        binding = ActivityAudioCallBinding.inflate(layoutInflater)

        setContentView(binding.root)

        // Audience row setup
        val audienceAdapter = GroupieAdapter()
//        binding.audienceRow.apply {
//            layoutManager = LinearLayoutManager(this@AudioCallActivity, LinearLayoutManager.HORIZONTAL, false)
//            adapter = audienceAdapter
//        }

        // speaker view setup
        val speakerAdapter = GroupieAdapter()
//        binding.speakerView.apply {
//            layoutManager = LinearLayoutManager(this@AudioCallActivity, LinearLayoutManager.HORIZONTAL, false)
//            adapter = speakerAdapter
//        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.participants.collect { participants ->
                    Log.d("Participants", participants.toString())

                    val localParticipant = participants.filterIsInstance<LocalParticipant>().firstOrNull()
                    val remoteParticipant = participants.filterIsInstance<Participant>()
                        .filterNot { it is LocalParticipant }
                        .firstOrNull()

                    Log.d("Participant", "LocalParticipant: $localParticipant")
                    Log.d("Participant", "RemoteParticipant: $remoteParticipant")

//                    val audienceItems = listOfNotNull(localParticipant).map {
//                        ParticipantItem(viewModel.room, it)
//                    }
//                    audienceAdapter.update(audienceItems)

                    if(remoteParticipant != null){
                        startCallTimer()
                    }else{
                        stopCallTimer()
                    }
//
//                    val speakerItems = listOfNotNull(remoteParticipant).map {
//                        ParticipantItem(viewModel.room, it)
//                    }
//                    speakerAdapter.update(speakerItems)
                }
            }
        }


//
//        lifecycleScope.launch {
//            repeatOnLifecycle(Lifecycle.State.CREATED) {
//                viewModel.primarySpeaker.collectLatest { speaker ->
//                    val items = listOfNotNull(speaker)
//                        .map { participant -> SpeakerItem(viewModel.room, participant) }
//                    speakerAdapter.update(items)
//                }
//            }
//        }

        // Controls setup
//        lifecycleScope.launch {
//            repeatOnLifecycle(Lifecycle.State.CREATED) {
//                viewModel.cameraEnabled.collect { enabled ->
//                    binding.camera.setOnClickListener { viewModel.setCameraEnabled(!enabled) }
//                    binding.camera.setImageResource(
//                        if (enabled) R.drawable.outline_videocam_24 else R.drawable.outline_videocam_off_24
//                    )
//                    binding.flipCamera.isEnabled = enabled
//                }
//            }
//        }

        // Microphone control
//        lifecycleScope.launch {
//            repeatOnLifecycle(Lifecycle.State.CREATED) {
//                viewModel.micEnabled.collect { enabled ->
//                    binding.mic.setOnClickListener { viewModel.setMicEnabled(!enabled) }
//                    binding.mic.setImageResource(
//                        if (enabled) R.drawable.outline_mic_24 else R.drawable.outline_mic_off_24
//                    )
//                }
//            }
//        }

//        binding.flipCamera.setOnClickListener { viewModel.flipCamera() }

        // Audio select
//        binding.audioSelect.setOnClickListener {
//            showSelectAudioDeviceDialog(viewModel)
//        }

        // Exit call
        binding.rejectButton.setOnClickListener {
            finish()
        }
    }

    private fun CoroutineScope.stopCallTimer() {
        if (isCallTimerRunning) {
            isCallTimerRunning = false
            handler.removeCallbacks(updateCallTimeRunnable) // Stop any pending updates
        }
    }

    private fun CoroutineScope.startCallTimer() {
        if (!isCallTimerRunning) {
            isCallTimerRunning = true
            callStartTimeMillis = System.currentTimeMillis() // Record the start time
            handler.post(updateCallTimeRunnable) // Start posting the runnable
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launchWhenResumed {
            viewModel.error.collect {
                it?.let {
                    Toast.makeText(this@AudioCallActivity, "Error: $it", Toast.LENGTH_LONG).show()
                    viewModel.dismissError()
                }
            }
        }

        lifecycleScope.launchWhenResumed {
            viewModel.dataReceived.collect {
                Toast.makeText(this@AudioCallActivity, "Data received: $it", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun requestMediaProjection() {
        val mediaProjectionManager =
            getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        screenCaptureIntentLauncher.launch(mediaProjectionManager.createScreenCaptureIntent())
    }

    override fun onDestroy() {
//        binding.audienceRow.adapter = null
//        binding.speakerView.adapter = null
        super.onDestroy()
    }

    companion object {
        const val KEY_ARGS = "args"
    }

    @Parcelize
    data class BundleArgs(
        val url: String,
        val token: String,
        val e2eeKey: String,
        val e2eeOn: Boolean,
        val stressTest: StressTest,
    ) : Parcelable
}
