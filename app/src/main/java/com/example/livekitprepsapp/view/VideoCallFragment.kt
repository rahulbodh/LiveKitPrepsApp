package com.example.livekitprepsapp.view

import android.annotation.SuppressLint
import android.app.Activity
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.livekitprepsapp.R
import com.example.livekitprepsapp.databinding.FragmentVideoCallBinding
import com.example.livekitprepsapp.dialog.showSelectAudioDeviceDialog
import com.example.livekitprepsapp.model.ParticipantItem
import com.example.livekitprepsapp.model.StressTest
import com.example.livekitprepsapp.viewModels.CallViewModel
import com.example.livekitprepsapp.viewModels.viewModelByFactory
import com.xwray.groupie.GroupieAdapter
import io.livekit.android.room.participant.LocalParticipant
import io.livekit.android.room.participant.Participant
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize

class VideoCallFragment : BaseCallFragment() {

    private var _binding: FragmentVideoCallBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: CallViewModel

    private val screenCaptureIntentLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val resultCode = result.resultCode
            val data = result.data
            if (resultCode != Activity.RESULT_OK || data == null) {
                return@registerForActivityResult
            }
            viewModel.startScreenCapture(data)
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        _binding = FragmentVideoCallBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("UnsafeRepeatOnLifecycleDetector")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val args = requireArguments().getParcelable<BundleArgs>("args")
            ?: throw IllegalArgumentException("args are required but were not provided")

        viewModel = viewModelByFactory {
            CallViewModel(
                url = args.url,
                token = args.token,
                e2ee = args.e2eeOn,
                e2eeKey = args.e2eeKey,
                stressTest = args.stressTest,
                application = requireActivity().application
            )
        }.value

        val audienceAdapter = GroupieAdapter()
        binding.audienceRow.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = audienceAdapter
        }

        val speakerAdapter = GroupieAdapter()
        binding.speakerView.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = speakerAdapter
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.participants.collect { participants ->
                    val localParticipant = participants.filterIsInstance<LocalParticipant>().firstOrNull()
                    val remoteParticipant = participants.filterIsInstance<Participant>()
                        .filterNot { it is LocalParticipant }
                        .firstOrNull()

                    val audienceItems = listOfNotNull(localParticipant).map {
                        ParticipantItem(viewModel.room, it)
                    }
                    audienceAdapter.update(audienceItems)

                    val speakerItems = listOfNotNull(remoteParticipant).map {
                        ParticipantItem(viewModel.room, it)
                    }
                    speakerAdapter.update(speakerItems)
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.cameraEnabled.collect { enabled ->
                    binding.camera.setOnClickListener { viewModel.setCameraEnabled(!enabled) }
                    binding.camera.setImageResource(
                        if (enabled) R.drawable.outline_videocam_24 else R.drawable.outline_videocam_off_24
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
                        if (enabled) R.drawable.outline_mic_24 else R.drawable.outline_mic_off_24
                    )
                }
            }
        }

        binding.flipCamera.setOnClickListener { viewModel.flipCamera() }
        binding.audioSelect.setOnClickListener { showSelectAudioDeviceDialog(viewModel) }
        binding.exit.setOnClickListener { requireActivity().finish() }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launchWhenResumed {
            viewModel.error.collect {
                it?.let {
                    Toast.makeText(requireContext(), "Error: $it", Toast.LENGTH_LONG).show()
                    viewModel.dismissError()
                }
            }
        }

        lifecycleScope.launchWhenResumed {
            viewModel.dataReceived.collect {
                Toast.makeText(requireContext(), "Data received: $it", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        binding.audienceRow.adapter = null
        binding.speakerView.adapter = null
        _binding = null
        super.onDestroyView()
    }

    private fun requestMediaProjection() {
        val mediaProjectionManager =
            requireContext().getSystemService(Activity.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        screenCaptureIntentLauncher.launch(mediaProjectionManager.createScreenCaptureIntent())
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
        val stressTest: StressTest
    ) : Parcelable
}
