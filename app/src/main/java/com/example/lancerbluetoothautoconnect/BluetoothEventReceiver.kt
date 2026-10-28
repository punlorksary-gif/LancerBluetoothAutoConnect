package com.example.lancerbluetoothautoconnect

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** Receives the system's ACL event when Android makes it available to this app. */
class BluetoothEventReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != BluetoothDevice.ACTION_ACL_CONNECTED || !Recovery.canReadBluetooth(context)) return
        val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE) ?: return
        try {
            if (device.name.equals(Recovery.DEVICE_NAME, ignoreCase = true)) {
                Recovery.showRecoveryNotification(context)
            }
        } catch (_: SecurityException) {
            // Permission was revoked after broadcast delivery.
        }
    }
}
