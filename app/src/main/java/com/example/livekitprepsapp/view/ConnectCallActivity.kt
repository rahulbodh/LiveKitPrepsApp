package com.example.livekitprepsapp.view

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.livekitprepsapp.R
import com.example.livekitprepsapp.databinding.ActivityConnectCallBinding

class ConnectCallActivity : AppCompatActivity() {

    private lateinit var binding : ActivityConnectCallBinding
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
    }
}