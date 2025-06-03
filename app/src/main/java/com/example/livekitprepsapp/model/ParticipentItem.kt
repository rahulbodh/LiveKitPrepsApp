package com.example.livekitprepsapp.model

import android.view.View
import com.example.livekitprepsapp.R
import com.example.livekitprepsapp.databinding.ParticipentItemBinding
import com.xwray.groupie.databinding.BindableItem
import com.xwray.groupie.databinding.GroupieViewHolder
import io.livekit.android.room.Room
import io.livekit.android.room.participant.ConnectionQuality
import io.livekit.android.room.participant.Participant
import io.livekit.android.room.track.CameraPosition
import io.livekit.android.room.track.LocalVideoTrack
import io.livekit.android.room.track.Track
import io.livekit.android.room.track.VideoTrack
import io.livekit.android.util.flow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber

class ParticipantItem(
    private val room: Room,
    private val participant: Participant,
    private val speakerView: Boolean = false,
) : BindableItem<ParticipentItemBinding>() {

    private var boundVideoTrack: VideoTrack? = null
    private var coroutineScope: CoroutineScope? = null

    override fun bind(viewBinding: ParticipentItemBinding, position: Int) {
        ensureCoroutineScope()

        // Initialize video renderer
        room.initVideoRenderer(viewBinding.renderer)

        // Observe participant identity
        coroutineScope?.launch {
            participant::identity.flow.collect { identity ->
                viewBinding.identityText.text = identity?.value ?: "Unknown"
            }
        }

        // Observe speaking state
        coroutineScope?.launch {
            participant::isSpeaking.flow.collect { isSpeaking ->
                if (isSpeaking) {
                    showFocus(viewBinding)
                } else {
                    hideFocus(viewBinding)
                }
            }
        }

        // Observe microphone state
        coroutineScope?.launch {
            participant::isMicrophoneEnabled.flow.collect { isMicEnabled ->
                viewBinding.muteIndicator.visibility = if (isMicEnabled) View.VISIBLE else View.INVISIBLE
            }
        }

        // Observe connection quality
        coroutineScope?.launch {
            participant::connectionQuality.flow.collect { quality ->
                viewBinding.connectionQuality.visibility =
                    if (quality == ConnectionQuality.POOR) View.VISIBLE else View.INVISIBLE
            }
        }

        // Observe video tracks changes
        val videoTrackPubFlow = participant::videoTrackPublications.flow
            .map { participant to it }
            .flatMapLatest { (participant, videoTracks) ->
                // Prioritize screenshare, then camera, then any available track
                val trackPublication = participant.getTrackPublication(Track.Source.SCREEN_SHARE)
                    ?: participant.getTrackPublication(Track.Source.CAMERA)
                    ?: videoTracks.firstOrNull()?.first

                flowOf(trackPublication)
            }

        coroutineScope?.launch {
            val videoTrackFlow = videoTrackPubFlow
                .flatMapLatestOrNull { pub -> pub::track.flow }

            // Configure video view with track
            launch {
                videoTrackFlow.collectLatest { videoTrack ->
                    setupVideoIfNeeded(videoTrack as? VideoTrack, viewBinding)
                }
            }

            // For local participants, mirror camera if using front camera
            if (participant == room.localParticipant) {
                launch {
                    videoTrackFlow
                        .flatMapLatestOrNull { track -> (track as? LocalVideoTrack)!!::options.flow }
                        .collectLatest { options ->
                            viewBinding.renderer.setMirror(options?.position == CameraPosition.FRONT)
                        }
                }
            }
        }

        // Handle muted state changes
        coroutineScope?.launch {
            videoTrackPubFlow
                .flatMapLatestOrNull { pub -> pub::muted.flow }
                .collectLatest { muted ->
                    viewBinding.renderer.visibleOrInvisible(!(muted ?: true))
                }
        }

        // Setup existing video track if available
        val existingTrack = getVideoTrack()
        if (existingTrack != null) {
            setupVideoIfNeeded(existingTrack, viewBinding)
        }
    }

    override fun unbind(viewHolder: GroupieViewHolder<ParticipentItemBinding>) {
        super.unbind(viewHolder)

        // Cancel coroutines
        coroutineScope?.cancel()
        coroutineScope = null

        // Clean up video track
        boundVideoTrack?.removeRenderer(viewHolder.binding.renderer)
        boundVideoTrack = null
    }

//    fun initializeViewBinding(view: View): ParticipentItemBinding {
//        return ParticipentItemBinding.bind(view)
//    }

    override fun getLayout(): Int = if (speakerView) {
        R.layout.speaker_view
    } else {
        R.layout.participent_item
    }

    // Private helper methods
    private fun ensureCoroutineScope() {
        if (coroutineScope == null) {
            coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
        }
    }

    private fun getVideoTrack(): VideoTrack? {
        return participant.getTrackPublication(Track.Source.CAMERA)?.track as? VideoTrack
    }

    private fun setupVideoIfNeeded(videoTrack: VideoTrack?, viewBinding: ParticipentItemBinding) {
        if (boundVideoTrack == videoTrack) {
            return
        }

        // Remove previous renderer
        boundVideoTrack?.removeRenderer(viewBinding.renderer)

        // Set new track and add renderer
        boundVideoTrack = videoTrack
        Timber.v("Adding renderer to $videoTrack")
        videoTrack?.addRenderer(viewBinding.renderer)
    }
}

// Extension functions
private fun View.visibleOrGone(visible: Boolean) {
    visibility = if (visible) View.VISIBLE else View.GONE
}

private fun View.visibleOrInvisible(visible: Boolean) {
    visibility = if (visible) View.VISIBLE else View.INVISIBLE
}

private fun showFocus(binding: ParticipentItemBinding) {
    binding.speakingIndicator.visibility = View.VISIBLE
}

private fun hideFocus(binding: ParticipentItemBinding) {
    binding.speakingIndicator.visibility = View.INVISIBLE
}

private inline fun <T, R> Flow<T?>.flatMapLatestOrNull(
    crossinline transform: suspend (value: T) -> Flow<R>,
): Flow<R?> {
    return flatMapLatest { value ->
        if (value == null) {
            flowOf(null)
        } else {
            transform(value)
        }
    }
}