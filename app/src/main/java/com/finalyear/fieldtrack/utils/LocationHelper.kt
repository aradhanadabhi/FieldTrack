package com.finalyear.fieldtrack.utils

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

/**
 * Wraps the fused location provider to fetch current GPS coordinates.
 * Caller must have already requested ACCESS_FINE_LOCATION at runtime.
 */
class LocationHelper(context: Context) {

    private val client = LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    fun getCurrentLocation(onResult: (lat: Double, lng: Double) -> Unit, onError: () -> Unit) {
        client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location ->
                if (location != null) {
                    onResult(location.latitude, location.longitude)
                } else {
                    onError()
                }
            }
            .addOnFailureListener { onError() }
    }
}
