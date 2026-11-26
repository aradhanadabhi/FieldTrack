package com.finalyear.fieldtrack.utils

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context

object BluetoothHelper {

    fun isBluetoothSupported(context: Context): Boolean {
        val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        return manager?.adapter != null
    }

    fun isBluetoothEnabled(context: Context): Boolean {
        val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        val adapter: BluetoothAdapter? = manager?.adapter
        return adapter?.isEnabled == true
    }

    fun getBluetoothStatusText(context: Context): String {
        if (!isBluetoothSupported(context)) return "Bluetooth: not supported"
        return if (isBluetoothEnabled(context)) "Bluetooth: ON" else "Bluetooth: OFF"
    }
}
