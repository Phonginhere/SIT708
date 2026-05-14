package com.example.p71.data.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.tasks.await

class LocationProvider(context: Context) {

    private val fusedClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context.applicationContext)

    /**
     * Returns the current device location, or null if unavailable.
     * Caller MUST have ACCESS_FINE_LOCATION (or ACCESS_COARSE_LOCATION) granted
     * before invoking this — the runtime permission check is done in the UI layer.
     */
    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): Location? = try {
        fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).await()
    } catch (e: Exception) {
        null
    }
}