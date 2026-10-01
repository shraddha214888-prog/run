package com.example.location

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.core.content.ContextCompat
import com.example.data.model.GeoPoint
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages real-time GPS location coordinates using Google Play Services Location API
 */
class GpsLocationManager(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val _currentLocation = MutableStateFlow(
        GeoPoint(latitude = 23.0338, longitude = 72.5186, label = "એસ.જી. હાઇવે, અમદાવાદ")
    )
    val currentLocation: StateFlow<GeoPoint> = _currentLocation.asStateFlow()

    private val _currentSpeedKmh = MutableStateFlow(55)
    val currentSpeedKmh: StateFlow<Int> = _currentSpeedKmh.asStateFlow()

    private val _currentBearingDegrees = MutableStateFlow(42f)
    val currentBearingDegrees: StateFlow<Float> = _currentBearingDegrees.asStateFlow()

    private val _isGpsActive = MutableStateFlow(false)
    val isGpsActive: StateFlow<Boolean> = _isGpsActive.asStateFlow()

    private var locationCallback: LocationCallback? = null

    fun hasLocationPermission(): Boolean {
        val fineLocation = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseLocation = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fineLocation || coarseLocation
    }

    @SuppressLint("MissingPermission")
    fun startLocationUpdates(onLocationChanged: ((Location) -> Unit)? = null) {
        if (!hasLocationPermission()) {
            _isGpsActive.value = false
            return
        }

        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { loc: Location? ->
                if (loc != null) {
                    updateLocationFromAndroid(loc)
                    onLocationChanged?.invoke(loc)
                }
            }

            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000L)
                .setMinUpdateIntervalMillis(1000L)
                .setWaitForAccurateLocation(false)
                .build()

            locationCallback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    val lastLoc = result.lastLocation ?: return
                    updateLocationFromAndroid(lastLoc)
                    onLocationChanged?.invoke(lastLoc)
                }
            }

            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback as LocationCallback,
                Looper.getMainLooper()
            )
            _isGpsActive.value = true
        } catch (e: Exception) {
            _isGpsActive.value = false
        }
    }

    private fun updateLocationFromAndroid(loc: Location) {
        _currentLocation.value = GeoPoint(
            latitude = loc.latitude,
            longitude = loc.longitude,
            label = "લાઇવ જીપીએસ લોકેશન"
        )
        if (loc.hasSpeed()) {
            _currentSpeedKmh.value = (loc.speed * 3.6f).toInt().coerceAtLeast(20)
        }
        if (loc.hasBearing()) {
            _currentBearingDegrees.value = loc.bearing
        }
    }

    fun stopLocationUpdates() {
        locationCallback?.let {
            fusedLocationClient.removeLocationUpdates(it)
        }
        locationCallback = null
        _isGpsActive.value = false
    }

    fun setSimulatedLocation(geoPoint: GeoPoint, speedKmh: Int, bearing: Float) {
        _currentLocation.value = geoPoint
        _currentSpeedKmh.value = speedKmh
        _currentBearingDegrees.value = bearing
    }
}
