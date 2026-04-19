package com.textcount

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.android.material.textfield.TextInputEditText

class MainActivity : AppCompatActivity() {

    private lateinit var prefs: TextCountPreferences
    private lateinit var tvCurrentCount: TextView
    private lateinit var tvTargetCount: TextView
    private lateinit var etTargetCount: TextInputEditText
    private lateinit var btnSetTarget: Button
    private lateinit var btnResetCount: Button
    private lateinit var tvNotificationAccessStatus: TextView
    private lateinit var btnGrantAccess: Button

    private val smsCountReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            updateCountDisplay()
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* no-op */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prefs = TextCountPreferences(this)

        tvCurrentCount = findViewById(R.id.tvCurrentCount)
        tvTargetCount = findViewById(R.id.tvTargetCount)
        etTargetCount = findViewById(R.id.etTargetCount)
        btnSetTarget = findViewById(R.id.btnSetTarget)
        btnResetCount = findViewById(R.id.btnResetCount)
        tvNotificationAccessStatus = findViewById(R.id.tvNotificationAccessStatus)
        btnGrantAccess = findViewById(R.id.btnGrantAccess)

        etTargetCount.setText(prefs.targetCount.toString())

        btnSetTarget.setOnClickListener {
            val input = etTargetCount.text.toString()
            val target = input.toIntOrNull()
            if (target != null && target > 0) {
                prefs.targetCount = target
                updateCountDisplay()
                Toast.makeText(this, "Target set to $target", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Please enter a number greater than 0", Toast.LENGTH_SHORT).show()
            }
        }

        btnGrantAccess.setOnClickListener {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }

        btnResetCount.setOnClickListener {
            prefs.resetCount()
            updateCountDisplay()
            Toast.makeText(this, "Count reset to 0", Toast.LENGTH_SHORT).show()
        }

        updateCountDisplay()
        requestPostNotificationsPermission()
    }

    override fun onResume() {
        super.onResume()
        val filter = IntentFilter(SmsNotificationListener.ACTION_SMS_COUNT_UPDATED)
        registerReceiver(smsCountReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        updateCountDisplay()
        updateNotificationAccessDisplay()
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(smsCountReceiver)
    }

    private fun updateCountDisplay() {
        val count = prefs.smsCount
        val target = prefs.targetCount
        tvCurrentCount.text = count.toString()
        tvTargetCount.text = "of $target"
    }

    private fun updateNotificationAccessDisplay() {
        val granted = NotificationManagerCompat.getEnabledListenerPackages(this).contains(packageName)
        tvNotificationAccessStatus.text = getString(
            if (granted) R.string.notification_access_granted else R.string.notification_access_not_granted
        )
        btnGrantAccess.isEnabled = !granted
    }

    private fun requestPostNotificationsPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
