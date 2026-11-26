package com.finalyear.fieldtrack.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.finalyear.fieldtrack.databinding.ActivityMainBinding
import com.finalyear.fieldtrack.network.SyncService
import com.finalyear.fieldtrack.utils.BluetoothHelper
import com.finalyear.fieldtrack.utils.WifiHelper

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val requiredPermissions = mutableListOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.CAMERA,
        Manifest.permission.SEND_SMS
    ).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            add(Manifest.permission.BLUETOOTH_CONNECT)
            add(Manifest.permission.BLUETOOTH_SCAN)
        }
    }.toTypedArray()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestNeededPermissions()
        refreshStatus()

        binding.btnAddVisit.setOnClickListener {
            startActivity(Intent(this, AddVisitActivity::class.java))
        }
        binding.btnViewVisits.setOnClickListener {
            startActivity(Intent(this, VisitListActivity::class.java))
        }
        binding.btnViewMap.setOnClickListener {
            startActivity(Intent(this, MapActivity::class.java))
        }
        binding.btnSyncNow.setOnClickListener {
            startService(Intent(this, SyncService::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        refreshStatus()
    }

    private fun refreshStatus() {
        binding.wifiStatusText.text = WifiHelper.getWifiStatusText(this)
        binding.bluetoothStatusText.text = BluetoothHelper.getBluetoothStatusText(this)
    }

    private fun requestNeededPermissions() {
        val missing = requiredPermissions.filter {
            ActivityCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (missing.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, missing.toTypedArray(), 100)
        }
    }
}
