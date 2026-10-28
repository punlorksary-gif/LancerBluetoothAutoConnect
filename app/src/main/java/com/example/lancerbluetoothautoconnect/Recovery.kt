package com.example.lancerbluetoothautoconnect

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat

object Recovery {
    const val DEVICE_NAME = "Hands Free System"
    private const val CHANNEL_ID = "lancer_recovery"
    private const val NOTIFICATION_ID = 14014

    fun canReadBluetooth(context: Context) = Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED

    fun matchingPairedDevice(context: Context): BluetoothDevice? {
        if (!canReadBluetooth(context)) return null
        return try {
            BluetoothAdapter.getDefaultAdapter()?.bondedDevices?.firstOrNull {
                it.name.equals(DEVICE_NAME, ignoreCase = true)
            }
        } catch (_: SecurityException) { null }
    }

    fun a2dpState(context: Context): String {
        if (!canReadBluetooth(context)) return "Bluetooth access has not been granted."
        return try {
            when (BluetoothAdapter.getDefaultAdapter()?.getProfileConnectionState(BluetoothProfile.A2DP)) {
                BluetoothProfile.STATE_CONNECTED -> "An A2DP media-audio profile is connected."
                BluetoothProfile.STATE_CONNECTING -> "A2DP media audio is connecting."
                BluetoothProfile.STATE_DISCONNECTING -> "A2DP media audio is disconnecting."
                else -> "No A2DP media-audio profile is currently connected."
            }
        } catch (_: SecurityException) { "Bluetooth access was denied." }
    }

    fun showRecoveryNotification(context: Context) {
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(context,
                Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(NotificationChannel(CHANNEL_ID,
            context.getString(com.example.lancerbluetoothautoconnect.R.string.channel_name),
            NotificationManager.IMPORTANCE_HIGH).apply {
            description = "Recovery actions when the Lancer connects"
        })
        val settingsIntent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
        val pending = PendingIntent.getActivity(context, 0, settingsIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_data_bluetooth)
            .setContentTitle("Lancer connected — check Media audio")
            .setContentText("Tap to open Bluetooth settings and enable Media audio for Hands Free System.")
            .setStyle(NotificationCompat.BigTextStyle().bigText(
                "Hands Free System connected. Android does not let third-party apps force A2DP; tap to check that Media audio is enabled."))
            .setContentIntent(pending).setAutoCancel(true).setPriority(NotificationCompat.PRIORITY_HIGH).build()
        manager.notify(NOTIFICATION_ID, notification)
    }
}
