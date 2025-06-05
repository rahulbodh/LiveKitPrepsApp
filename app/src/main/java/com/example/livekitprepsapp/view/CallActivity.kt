package com.example.livekitprepsapp.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.livekitprepsapp.R
import com.example.livekitprepsapp.databinding.ActivityCallBinding
import com.example.livekitprepsapp.viewModels.CallViewModel

import android.app.Activity
import android.media.projection.MediaProjectionManager
import android.os.Parcelable
import android.util.Log
import android.view.WindowManager
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.livekitprepsapp.dialog.showAudioProcessorSwitchDialog
import com.example.livekitprepsapp.dialog.showDebugMenuDialog
import com.example.livekitprepsapp.dialog.showSelectAudioDeviceDialog
import com.example.livekitprepsapp.model.ParticipantItem
import com.example.livekitprepsapp.model.SpeakerItem
import com.example.livekitprepsapp.model.StressTest
import com.example.livekitprepsapp.viewModels.viewModelByFactory
import com.xwray.groupie.GroupieAdapter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize

class CallActivity : AppCompatActivity() {

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
        )

    }


    private lateinit var binding: ActivityCallBinding
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
        binding = ActivityCallBinding.inflate(layoutInflater)

        setContentView(binding.root)

        // Audience row setup
        val audienceAdapter = GroupieAdapter()
        binding.audienceRow.apply {
            layoutManager = LinearLayoutManager(this@CallActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = audienceAdapter
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.participants
                    .collect { participants ->
                        val items = participants.map { participant -> ParticipantItem(viewModel.room, participant) }
                        audienceAdapter.update(items)
                    }
            }
        }

        // speaker view setup
        val speakerAdapter = GroupieAdapter()
        binding.speakerView.apply {
            layoutManager = LinearLayoutManager(this@CallActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = speakerAdapter
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.primarySpeaker.collectLatest { speaker ->
                    val items = listOfNotNull(speaker)
                        .map { participant -> SpeakerItem(viewModel.room, participant) }
                    speakerAdapter.update(items)
                }
            }
        }

        // Controls setup
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.cameraEnabled.collect { enabled ->
                    binding.camera.setOnClickListener { viewModel.setCameraEnabled(!enabled) }
                    binding.camera.setImageResource(
                        if (enabled) {
                            R.drawable.outline_videocam_24
                        } else {
                            R.drawable.outline_videocam_off_24
                        },
                    )
                    binding.flipCamera.isEnabled = enabled
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.micEnabled.collect { enabled ->
                    binding.mic.setOnClickListener { viewModel.setMicEnabled(!enabled) }
                    binding.mic.setImageResource(
                        if (enabled) {
                            R.drawable.outline_mic_24
                        } else {
                            R.drawable.outline_mic_off_24
                        },
                    )
                }
            }
        }

        binding.flipCamera.setOnClickListener { viewModel.flipCamera() }

    }



    override fun onResume() {
        super.onResume()
        lifecycleScope.launchWhenResumed {
            viewModel.error.collect {
                if (it != null) {
                    Toast.makeText(this@CallActivity, "Error: $it", Toast.LENGTH_LONG).show()
                    Log.e("TAGY", "Error : ${it.message.toString()}")
                    viewModel.dismissError()
                }
            }
        }

        lifecycleScope.launchWhenResumed {
            viewModel.dataReceived.collect {
                Toast.makeText(this@CallActivity, "Data received: $it", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroy() {
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