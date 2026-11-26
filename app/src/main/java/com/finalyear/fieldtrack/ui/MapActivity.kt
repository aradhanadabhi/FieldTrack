package com.finalyear.fieldtrack.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import com.finalyear.fieldtrack.R
import com.finalyear.fieldtrack.db.DatabaseHelper
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions

/**
 * Shows every saved visit as a marker on Google Maps.
 * Demonstrates "Using Location based APIs including Google Maps".
 */
class MapActivity : FragmentActivity(), OnMapReadyCallback {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        val db = DatabaseHelper.getInstance(this)
        val visits = db.getAllVisits().filter { it.latitude != 0.0 || it.longitude != 0.0 }

        if (visits.isEmpty()) return

        val boundsBuilder = LatLngBounds.Builder()
        for (visit in visits) {
            val position = LatLng(visit.latitude, visit.longitude)
            googleMap.addMarker(
                MarkerOptions().position(position).title(visit.siteName).snippet(visit.notes)
            )
            boundsBuilder.include(position)
        }
        googleMap.moveCamera(
            CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 100)
        )
    }
}
