package com.example.livekitprepsapp.view

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.livekitprepsapp.R
import com.example.livekitprepsapp.databinding.ActivityMainBinding
import com.example.livekitprepsapp.model.StressTest
import com.example.livekitprepsapp.utils.ForegroundService
import com.example.livekitprepsapp.viewModels.MainViewModel
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding
    private val viewModel by viewModels<MainViewModel>()
    private val PERMISSION_REQUEST_CODE = 100
    private val REQUIRED_PERMISSIONS = arrayOf(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.CAMERA,
        Manifest.permission.POST_NOTIFICATIONS
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val urlString = viewModel.getSavedUrl()
        val tokenString = viewModel.getSavedToken()
        val e2EEOn = viewModel.getE2EEOptionsOn()
        val e2EEKey = viewModel.getSavedE2EEKey()

        val serviceIntent = Intent(this, ForegroundService::class.java).apply {
            putExtra("callerName", "Alice")
            putExtra("isIncomingCall", true)
        }
        ContextCompat.startForegroundService(this, serviceIntent)


        binding.run {
            url.editText?.text = SpannableStringBuilder(urlString)
            token.editText?.text = SpannableStringBuilder(tokenString)
            e2eeEnabled.isChecked = e2EEOn
            e2eeKey.editText?.text = SpannableStringBuilder(e2EEKey)


            connectButton.setOnClickListener {
//                val intent = Intent(this@MainActivity, CallActivity::class.java).apply {
//                    putExtra(
//                        CallActivity.KEY_ARGS,
//                        CallActivity.BundleArgs(
//                            url = url.editText?.text.toString(),
//                            token = token.editText?.text.toString(),
//                            e2eeOn = e2eeEnabled.isChecked,
//                            e2eeKey = e2eeKey.editText?.text.toString(),
//                            stressTest = StressTest.None,
//                        ),
//                    )
//                }

                val intent = Intent(this@MainActivity, ConnectCallActivity::class.java)

                startActivity(intent)
            }

            saveButton.setOnClickListener {
                viewModel.setSavedUrl(url.editText?.text?.toString() ?: "")
                viewModel.setSavedToken(token.editText?.text?.toString() ?: "")
                viewModel.setSavedE2EEOn(e2eeEnabled.isChecked)
                viewModel.setSavedE2EEKey(e2eeKey.editText?.text?.toString() ?: "")

                Toast.makeText(
                    this@MainActivity,
                    "Values saved.",
                    Toast.LENGTH_SHORT,
                ).show()
            }

            resetButton.setOnClickListener {
                viewModel.reset()
                url.editText?.text = SpannableStringBuilder(MainViewModel.URL)
                token.editText?.text = SpannableStringBuilder(MainViewModel.TOKEN)
                e2eeEnabled.isChecked = false
                e2eeKey.editText?.text = SpannableStringBuilder("")

                Toast.makeText(
                    this@MainActivity,
                    "Values reset.",
                    Toast.LENGTH_SHORT,
                ).show()
            }
        }
        getFCMToken()
        requestNeededPermissions()

    }


    fun getFCMToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCM", "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result
            Log.d("FCM", "Current FCM Token: $token")

            // You would typically send this token to your backend server now
            // sendRegistrationToServer(token)
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            val deniedPermissions = permissions.zip(grantResults.toTypedArray())
                .filter { it.second != PackageManager.PERMISSION_GRANTED }
                .map { it.first }

            if (deniedPermissions.isEmpty()) {
                // All permissions granted
            } else {
                // Some permissions denied — show rationale or handle appropriately
            }
        }
    }

    private fun requestNeededPermissions() {
        val missingPermissions = REQUIRED_PERMISSIONS.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                missingPermissions.toTypedArray(),
                PERMISSION_REQUEST_CODE
            )
        }
    }
}