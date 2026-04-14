package com.textcount
 
import android.Manifest
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.textfield.TextInputEditText
 
class MainActivity : AppCompatActivity() {
 
    private lateinit var prefs: TextCountPreferences
    private lateinit var tvCurrentCount: TextView
    private lateinit var tvTargetCount: TextView
    private lateinit var etTargetCount: TextInputEditText
    private lateinit var tvSelectedRingtone: TextView
    private lateinit var btnSetTarget: Button
    private lateinit var btnChooseRingtone: Button
    private lateinit var btnResetCount: Button
 
    private val ringtonePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri: Uri? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                result.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI, Uri::class.java)
            } else {
                @Suppress("DEPRECATION")
                result.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            }
            if (uri != null) {
                prefs.ringtoneUri = uri.toString()
                updateRingtoneDisplay()
            }
        }
    }
 
    private val smsCountReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            updateCountDisplay()
        }
    }
 
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val smsGranted = permissions[Manifest.permission.RECEIVE_SMS] == true
        if (!smsGranted) {
            Toast.makeText(
                this,
                "SMS permission is required to count incoming texts",
                Toast.LENGTH_LONG
            ).show()
        }
    }
 
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
 
        prefs = TextCountPreferences(this)
 
        tvCurrentCount = findViewById(R.id.tvCurrentCount)
        tvTargetCount = findViewById(R.id.tvTargetCount)
        etTargetCount = findViewById(R.id.etTargetCount)
        tvSelectedRingtone = findViewById(R.id.tvSelectedRingtone)
        btnSetTarget = findViewById(R.id.btnSetTarget)
        btnChooseRingtone = findViewById(R.id.btnChooseRingtone)
        btnResetCount = findViewById(R.id.btnResetCount)
 
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
 
        btnChooseRingtone.setOnClickListener {
            val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALL)
                putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Tone")
                putExtra(
                    RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,
                    Uri.parse(prefs.ringtoneUri)
                )
                putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
                putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
            }
            ringtonePickerLauncher.launch(intent)
        }
 
        btnResetCount.setOnClickListener {
            prefs.resetCount()
            updateCountDisplay()
            Toast.makeText(this, "Count reset to 0", Toast.LENGTH_SHORT).show()
        }
 
        updateCountDisplay()
        updateRingtoneDisplay()
        requestPermissions()
    }
 
    override fun onResume() {
        super.onResume()
        val filter = IntentFilter(SmsReceiver.ACTION_SMS_COUNT_UPDATED)
        registerReceiver(smsCountReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        updateCountDisplay()
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
 
    private fun updateRingtoneDisplay() {
        try {
            val uri = Uri.parse(prefs.ringtoneUri)
            val ringtone = RingtoneManager.getRingtone(this, uri)
            tvSelectedRingtone.text = ringtone?.getTitle(this) ?: "Default"
        } catch (e: Exception) {
            tvSelectedRingtone.text = "Default"
        }
    }
 
    private fun requestPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
 
        val needed = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
 
        if (needed.isNotEmpty()) {
            requestPermissionLauncher.launch(needed.toTypedArray())
        }
    }
}
