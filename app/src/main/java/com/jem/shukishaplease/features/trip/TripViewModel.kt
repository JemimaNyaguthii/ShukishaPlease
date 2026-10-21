package com.jem.shukishaplease.features.trip

import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.*
import com.jem.shukishaplease.core.common.CommuterPrefs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.*

data class ActiveTripUiState(
    val currentPlaceName: String = "Locating…",
    val destinationName: String = "",
    val startPlaceName: String = "Origin",
    val midPlaceName: String = "Midpoint",
    val distanceLeftKm: Float = 0f,
    val totalDistanceKm: Float = 0f,
    val etaMinutes: Int = 0,
    val etaDisplayText: String = "0 min",
    val arrivalTime: String = "--:--",
    val progress: Float = 0f,
    val gpsStatus: String = "GPS searching",
    val currentLat: Double = 0.0,
    val currentLon: Double = 0.0,
    val speedMps: Float = 0f,
    val accuracyMeters: Float = 0f,
    val isTrackingActive: Boolean = false
)

class TripViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ActiveTripUiState())
    val uiState: StateFlow<ActiveTripUiState> = _uiState.asStateFlow()

    private var fusedClient: FusedLocationProviderClient? = null
    private var locationCallback: LocationCallback? = null

    private var resolvedDestLat: Double = 0.0
    private var resolvedDestLon: Double = 0.0
    private var initialStartLat: Double? = null
    private var initialStartLon: Double? = null
    private var totalTripDistanceKm: Float = 0f
    private val speedHistory = mutableListOf<Float>()

    private var lastGeocodedLat: Double = 0.0
    private var lastGeocodedLon: Double = 0.0
    private var lastLocationUpdateMs: Long = 0L

    fun startTripTracking(
        context: Context,
        destName: String,
        inputLat: Double,
        inputLon: Double
    ) {
        _uiState.value = _uiState.value.copy(
            destinationName = destName,
            gpsStatus = "GPS searching",
            isTrackingActive = true
        )

        val prefs = CommuterPrefs(context)
        prefs.isTripActive = true
        prefs.activeTripDestination = destName

        startGpsWatchdog()

        viewModelScope.launch(Dispatchers.IO) {
            // Step 1: Geocode destination if coordinates are missing/zero
            if (inputLat == 0.0 || inputLon == 0.0) {
                try {
                    val geocoder = Geocoder(context, Locale.getDefault())
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        geocoder.getFromLocationName(destName, 1) { addresses ->
                            val addr = addresses.firstOrNull()
                            if (addr != null) {
                                resolvedDestLat = addr.latitude
                                resolvedDestLon = addr.longitude
                                Log.d("TripDestinationFlow", "Geocoded '$destName' to ($resolvedDestLat, $resolvedDestLon)")
                            }
                        }
                    } else {
                        @Suppress("DEPRECATION")
                        val addresses = geocoder.getFromLocationName(destName, 1)
                        val addr = addresses?.firstOrNull()
                        if (addr != null) {
                            resolvedDestLat = addr.latitude
                            resolvedDestLon = addr.longitude
                            Log.d("TripDestinationFlow", "Geocoded '$destName' to ($resolvedDestLat, $resolvedDestLon)")
                        }
                    }
                } catch (e: Exception) {
                    Log.e("TripDestinationFlow", "Failed to geocode destination '$destName': ${e.localizedMessage}")
                }
            } else {
                resolvedDestLat = inputLat
                resolvedDestLon = inputLon
            }

            // Fallback default coordinates if geocoding yields zero
            if (resolvedDestLat == 0.0 || resolvedDestLon == 0.0) {
                resolvedDestLat = -0.4201
                resolvedDestLon = 36.9476
            }

            // Capture initial start location from lastKnownLocation cache only if fresh (<60s) and accurate (<=100m)
            fusedClient = LocationServices.getFusedLocationProviderClient(context)
            try {
                fusedClient?.lastLocation?.addOnSuccessListener { startLoc ->
                    if (startLoc != null) {
                        val isFresh = (System.currentTimeMillis() - startLoc.time) <= 60000L
                        val isAccurate = !startLoc.hasAccuracy() || startLoc.accuracy <= 100f

                        if (isFresh && isAccurate) {
                            _uiState.value = _uiState.value.copy(
                                currentLat = startLoc.latitude,
                                currentLon = startLoc.longitude
                            )
                            Log.d(
                                "TripDestinationFlow",
                                "Trip Started | Fresh Last Known Location: (${startLoc.latitude}, ${startLoc.longitude}) -> Destination: '$destName' ($resolvedDestLat, $resolvedDestLon)"
                            )
                        } else {
                            Log.w(
                                "TripDestinationFlow",
                                "Ignored stale/inaccurate last known location (Fresh: $isFresh, Accurate: $isAccurate, Accuracy: ${startLoc.accuracy}m)"
                            )
                        }
                    }
                }
            } catch (ignored: SecurityException) {}

            // Step 2: Begin live Fused Location Provider updates (every 3-5 seconds)
            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 4000L)
                .setMinUpdateIntervalMillis(2500L)
                .build()

            locationCallback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    for (location in result.locations) {
                        processLocationUpdate(context, location, destName)
                    }
                }
            }

            try {
                fusedClient?.requestLocationUpdates(locationRequest, locationCallback!!, context.mainLooper)
            } catch (e: SecurityException) {
                Log.e("TripDestinationFlow", "Location permission denied: ${e.localizedMessage}")
                _uiState.value = _uiState.value.copy(gpsStatus = "GPS permission denied")
            }
        }
    }

    private fun startGpsWatchdog() {
        viewModelScope.launch(Dispatchers.Default) {
            while (_uiState.value.isTrackingActive) {
                delay(5000)
                if (lastLocationUpdateMs > 0L) {
                    val timeSinceUpdateMs = System.currentTimeMillis() - lastLocationUpdateMs
                    if (timeSinceUpdateMs > 180000L) { // 3 minutes
                        _uiState.value = _uiState.value.copy(gpsStatus = "LOST")
                    } else if (timeSinceUpdateMs > 60000L) { // 60 seconds
                        _uiState.value = _uiState.value.copy(gpsStatus = "GPS weak")
                    }
                }
            }
        }
    }

    private fun processLocationUpdate(
        context: Context,
        location: Location,
        destName: String
    ) {
        lastLocationUpdateMs = System.currentTimeMillis()

        val currLat = location.latitude
        val currLon = location.longitude

        // Capture initial trip starting location once
        if (initialStartLat == null || initialStartLon == null) {
            initialStartLat = currLat
            initialStartLon = currLon
            geocodeOriginAndMidpointOnce(context, currLat, currLon, resolvedDestLat, resolvedDestLon, destName)
        }

        // 1. Road Distance Left in km (Haversine multiplied by 1.25x Road Tortuosity Factor for realistic road routing)
        val distanceLeftKm = haversineDistanceKm(currLat, currLon, resolvedDestLat, resolvedDestLon)

        // 2. Total Distance Calculated once at trip start
        if (totalTripDistanceKm <= 0f || distanceLeftKm > totalTripDistanceKm) {
            totalTripDistanceKm = distanceLeftKm.coerceAtLeast(0.1f)
        }

        // 3. Progress: 1 - (distanceLeft / totalDistance), clamped 0..1
        val calculatedProgress = (1f - (distanceLeftKm / totalTripDistanceKm)).coerceIn(0f, 1f)

        // 4. Speed Smoothing (last ~30 seconds)
        if (location.hasSpeed() && location.speed > 0.5f) {
            speedHistory.add(location.speed)
            if (speedHistory.size > 8) speedHistory.removeAt(0)
        }

        // Route-Type Commute Baseline & Traffic Multiplier
        val isUrbanRoute = destName.contains("Westlands", true) ||
                destName.contains("CBD", true) ||
                destName.contains("Kasarani", true) ||
                destName.contains("Kilimani", true) ||
                destName.contains("Market", true)

        val routeBaselineMinsPerKm = if (isUrbanRoute) 3.5f else 1.25f

        val liveSpeedKmh = if (speedHistory.isNotEmpty()) (speedHistory.average().toFloat() * 3.6f) else 0f
        val trafficMultiplier = when {
            liveSpeedKmh in 1f..15f -> 1.75f // Heavy traffic slowdown
            liveSpeedKmh in 15f..35f -> 1.30f // Slow commute traffic
            else -> 1.0f // Normal flow
        }

        val stageStopAllowanceMins = if (isUrbanRoute) 4 else 8

        // Real-World Commute ETA Calculation
        val rawEtaMins = (distanceLeftKm * routeBaselineMinsPerKm * trafficMultiplier).roundToInt() + stageStopAllowanceMins
        val etaMinutes = rawEtaMins.coerceAtLeast(1)

        // Sync live ETA and destination to CommuterPrefs for Play Hub
        val prefs = CommuterPrefs(context)
        prefs.isTripActive = true
        prefs.activeTripDestination = destName
        prefs.activeTripEtaMinutes = etaMinutes

        // 5. ETA Display Text: "45 min" if under 60, otherwise "1 hr 20 min"
        val etaDisplayText = if (etaMinutes >= 60) {
            val hrs = etaMinutes / 60
            val mins = etaMinutes % 60
            if (mins > 0) "$hrs hr $mins min" else "$hrs hr"
        } else {
            "$etaMinutes min"
        }

        // Arrival Time Formatting
        val cal = Calendar.getInstance()
        cal.add(Calendar.MINUTE, etaMinutes)
        val arrivalTimeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(cal.time)

        // GPS Accuracy Pill
        val accuracyMeters = location.accuracy
        val gpsStatusStr = when {
            accuracyMeters <= 20f -> "GPS strong"
            accuracyMeters <= 50f -> "GPS fair"
            else -> "GPS searching"
        }

        // Geocode current place name only when moved > 500m
        resolveCurrentPlaceName(context, currLat, currLon)

        _uiState.value = _uiState.value.copy(
            destinationName = destName,
            distanceLeftKm = distanceLeftKm,
            totalDistanceKm = totalTripDistanceKm,
            etaMinutes = etaMinutes,
            etaDisplayText = etaDisplayText,
            arrivalTime = arrivalTimeStr,
            progress = calculatedProgress,
            gpsStatus = gpsStatusStr,
            currentLat = currLat,
            currentLon = currLon,
            speedMps = location.speed,
            accuracyMeters = accuracyMeters
        )

        Log.d(
            "ActiveTripLiveData",
            "DistanceLeft: ${"%.2f".format(distanceLeftKm)} km | TotalDist: ${"%.2f".format(totalTripDistanceKm)} km | TrafficMult: ${"%.2f".format(trafficMultiplier)} | Progress: ${"%.2f".format(calculatedProgress)} | ETA: $etaDisplayText"
        )
    }

    private fun geocodeOriginAndMidpointOnce(
        context: Context,
        startLat: Double,
        startLon: Double,
        destLat: Double,
        destLon: Double,
        destName: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())

                // Origin Stage (Trip Start)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    geocoder.getFromLocation(startLat, startLon, 1) { addresses ->
                        val addr = addresses.firstOrNull()
                        val name = addr?.subLocality ?: addr?.locality ?: addr?.thoroughfare ?: "Origin Stage"
                        _uiState.value = _uiState.value.copy(startPlaceName = name, currentPlaceName = name)
                    }
                } else {
                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocation(startLat, startLon, 1)
                    val addr = addresses?.firstOrNull()
                    val name = addr?.subLocality ?: addr?.locality ?: addr?.thoroughfare ?: "Origin Stage"
                    _uiState.value = _uiState.value.copy(startPlaceName = name, currentPlaceName = name)
                }

                // Midpoint Stage (Halfway between origin & destination)
                val midLat = (startLat + destLat) / 2.0
                val midLon = (startLon + destLon) / 2.0

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    geocoder.getFromLocation(midLat, midLon, 1) { midAddresses ->
                        val midAddr = midAddresses.firstOrNull()
                        val midName = midAddr?.subLocality ?: midAddr?.locality ?: midAddr?.thoroughfare ?: "Midpoint"
                        _uiState.value = _uiState.value.copy(midPlaceName = midName)
                    }
                } else {
                    @Suppress("DEPRECATION")
                    val midAddresses = geocoder.getFromLocation(midLat, midLon, 1)
                    val midAddr = midAddresses?.firstOrNull()
                    val midName = midAddr?.subLocality ?: midAddr?.locality ?: midAddr?.thoroughfare ?: "Midpoint"
                    _uiState.value = _uiState.value.copy(midPlaceName = midName)
                }
            } catch (e: Exception) {
                val fallbackOrigin = if (destName.contains("Nyeri", true)) "Karatina" else "CBD Stage"
                val fallbackMid = if (destName.contains("Nyeri", true)) "Kiganjo" else "Museum Hill"
                _uiState.value = _uiState.value.copy(startPlaceName = fallbackOrigin, midPlaceName = fallbackMid)
            }
        }
    }

    private fun resolveCurrentPlaceName(
        context: Context,
        currLat: Double,
        currLon: Double
    ) {
        val distMovedKm = if (lastGeocodedLat != 0.0) haversineDistanceKm(lastGeocodedLat, lastGeocodedLon, currLat, currLon) else 10f
        if (distMovedKm < 0.5f && _uiState.value.currentPlaceName != "Locating…") {
            return
        }

        lastGeocodedLat = currLat
        lastGeocodedLon = currLon

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    geocoder.getFromLocation(currLat, currLon, 1) { addresses ->
                        val addr = addresses.firstOrNull()
                        val name = addr?.subLocality ?: addr?.locality ?: addr?.thoroughfare ?: _uiState.value.currentPlaceName
                        _uiState.value = _uiState.value.copy(currentPlaceName = name)
                    }
                } else {
                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocation(currLat, currLon, 1)
                    val addr = addresses?.firstOrNull()
                    val name = addr?.subLocality ?: addr?.locality ?: addr?.thoroughfare ?: _uiState.value.currentPlaceName
                    _uiState.value = _uiState.value.copy(currentPlaceName = name)
                }
            } catch (e: Exception) {
                // Ignore geocoder network failures
            }
        }
    }

    private fun haversineDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
        val r = 6371.0 // Earth radius in kilometers
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        val straightLineKm = (r * c).toFloat()
        // Apply Road Tortuosity Factor (~1.25x) to convert straight-line distance into realistic road distance
        return straightLineKm * 1.25f
    }

    fun stopTracking() {
        if (fusedClient != null && locationCallback != null) {
            try {
                fusedClient?.removeLocationUpdates(locationCallback!!)
            } catch (e: Exception) {
                Log.e("TripDestinationFlow", "Failed to stop location updates: ${e.localizedMessage}")
            }
        }
        _uiState.value = _uiState.value.copy(isTrackingActive = false)
    }

    override fun onCleared() {
        super.onCleared()
        stopTracking()
    }
}
