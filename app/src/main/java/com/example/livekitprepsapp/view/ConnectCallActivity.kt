package com.example.livekitprepsapp.view

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.livekitprepsapp.R
import com.example.livekitprepsapp.databinding.ActivityConnectCallBinding
import com.example.livekitprepsapp.model.StressTest
import com.example.livekitprepsapp.viewModels.MainViewModel
import kotlin.getValue

class ConnectCallActivity : AppCompatActivity() {

    private lateinit var binding: ActivityConnectCallBinding
    private val viewModel by viewModels<MainViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityConnectCallBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val imageUrl = "https://images.pexels.com/photos/1391498/pexels-photo-1391498.jpeg"
        Glide.with(this)
            .load(imageUrl)
            .apply(RequestOptions().circleCrop())
            .into(binding.callerImage)

        val urlString = viewModel.getSavedUrl()
        val tokenString = viewModel.getSavedToken()
        val e2EEOn = viewModel.getE2EEOptionsOn()
        val e2EEKey = viewModel.getSavedE2EEKey()

//        val intent = Intent(this@ConnectCallActivity, VideoCallActivity::class.java).apply {
//            putExtra(
//                VideoCallActivity.KEY_ARGS,
//                VideoCallActivity.BundleArgs(
//                    url = urlString,
//                    token = tokenString,
//                    e2eeOn = e2EEOn,
//                    e2eeKey = e2EEKey,
//                    stressTest = StressTest.None,
//                ),
//            )
//                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
//        }

        Handler().postDelayed(
            {
                startActivity(intent)
            },
            3000
        )


    }
}