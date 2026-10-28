package com.example.lancerbluetoothautoconnect

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    private lateinit var status: TextView
    private val permissionRequest = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { updateStatus() }
    private val liveReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == BluetoothDevice.ACTION_ACL_CONNECTED) updateStatus()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        status = findViewById(R.id.status)
        findViewById<Button>(R.id.grantPermissions).setOnClickListener { requestNeededPermissions() }
        findViewById<Button>(R.id.refresh).setOnClickListener { updateStatus() }
        findViewById<Button>(R.id.openBluetooth).setOnClickListener {
            startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS))
        }
        updateStatus()
    }

    override fun onStart() {
        super.onStart()
        ContextCompat.registerReceiver(this, liveReceiver, IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED),
            ContextCompat.RECEIVER_NOT_EXPORTED)
    }
    override fun onStop() { unregisterReceiver(liveReceiver); super.onStop() }

    private fun requestNeededPermissions() {
        val wanted = buildList {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && ContextCompat.checkSelfPermission(this@MainActivity,
                    Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) add(Manifest.permission.BLUETOOTH_CONNECT)
            if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(this@MainActivity,
                    Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) add(Manifest.permission.POST_NOTIFICATIONS)
        }
        if (wanted.isNotEmpty()) permissionRequest.launch(wanted.toTypedArray()) else updateStatus()
    }

    private fun updateStatus() {
        if (!Recovery.canReadBluetooth(this)) {
            status.text = "Bluetooth permission is needed to identify paired devices and receive useful diagnostics."
            return
        }
        val device = Recovery.matchingPairedDevice(this)
        status.text = buildString {
            append(if (device == null) "Paired device ‘${Recovery.DEVICE_NAME}’ was not found." else
                "Paired device found: ${device.name} (${device.address}).")
            append("\n\n${Recovery.a2dpState(this@MainActivity)}")
            append("\n\nAutomatic recovery: a system connection event for this device posts an action notification when Android delivers it. The notification opens Bluetooth settings; this app cannot programmatically connect A2DP.")
        }
    }
}
