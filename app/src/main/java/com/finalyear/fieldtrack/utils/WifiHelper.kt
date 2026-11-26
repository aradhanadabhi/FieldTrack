package com.finalyear.fieldtrack.utils

import android.content.Context
import android.net.wifi.WifiManager

object WifiHelper {

    fun isWifiEnabled(context: Context): Boolean {
        val wifiManager = context.applicationContext
            .getSystemService(Context.WIFI_SERVICE) as WifiManager
        return wifiManager.isWifiEnabled
    }

    fun getWifiStatusText(context: Context): String {
        return if (isWifiEnabled(context)) "Wi-Fi: ON" else "Wi-Fi: OFF"
    }
}
