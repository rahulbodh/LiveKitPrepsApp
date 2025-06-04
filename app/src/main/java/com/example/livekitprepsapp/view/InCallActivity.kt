package com.example.livekitprepsapp.view

import android.content.Intent
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.livekitprepsapp.R
import com.example.livekitprepsapp.databinding.ActivityInCallBinding

class InCallActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInCallBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityInCallBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.acceptButton.setOnClickListener {
           startActivity(Intent(this, CallActivity::class.java))
        }

        binding.rejectButton.setOnClickListener {
            finish()
        }


    }
}