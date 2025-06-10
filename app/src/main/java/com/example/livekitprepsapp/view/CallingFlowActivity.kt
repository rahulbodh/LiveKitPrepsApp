package com.example.livekitprepsapp.view

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.livekitprepsapp.R
import com.example.livekitprepsapp.databinding.ActivityCallingFlowBinding
import com.example.livekitprepsapp.model.CallArgs

class CallingFlowActivity : AppCompatActivity() {

    private lateinit var binding : ActivityCallingFlowBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCallingFlowBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val args = intent.getParcelableExtra<CallArgs>("args")

        if (savedInstanceState == null && args != null) {
            val fragment: Fragment = when (args.callType) {
                "video" -> VideoCallFragment()
                "audio" -> AudioCallFragment()
                else -> throw IllegalArgumentException("Unknown call type")
            }
            fragment.arguments = bundleOf("args" to args)
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container_view, fragment)
                .commit()
        }
    }
}