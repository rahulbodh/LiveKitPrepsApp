package com.example.livekitprepsapp.model

import com.example.livekitprepsapp.R
import com.example.livekitprepsapp.databinding.SpeakerViewBinding
import com.xwray.groupie.databinding.BindableItem
import io.livekit.android.room.Room
import io.livekit.android.room.participant.Participant

class SpeakerItem(
    private val room: Room,
    private val participant: Participant
) : BindableItem<SpeakerViewBinding>() {

    override fun getLayout(): Int = R.layout.speaker_view

    override fun bind(viewBinding: SpeakerViewBinding, position: Int) {
        // Duplicate or refactor shared logic for speaker layout
    }
}