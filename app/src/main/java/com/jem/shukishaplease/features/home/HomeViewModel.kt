package com.jem.shukishaplease.features.home

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.jem.shukishaplease.core.model.Destination
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Idle)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _selectedDestination = MutableStateFlow<Destination?>(null)
    val selectedDestination: StateFlow<Destination?> = _selectedDestination.asStateFlow()

    val sampleDestinations = listOf(
        Destination("Nyeri Town", -0.4201, 36.9476, "Nyeri County, Kenya"),
        Destination("Karatina", -0.4849, 37.1264, "Nyeri-Karatina Rd"),
        Destination("CBD / General Post Office", -1.286389, 36.817223, "Haile Selassie Ave"),
        Destination("Westlands / Sarit Centre", -1.2618, 36.8055, "Waiyaki Way"),
        Destination("Karen / The Hub", -1.3195, 36.7073, "Karen Rd"),
        Destination("Kasarani / Mwiki", -1.2217, 36.8967, "Thika Rd")
    )

    fun selectDestination(destination: Destination) {
        _selectedDestination.value = destination
    }

    fun searchDestinationByName(context: Context, query: String, onResult: (Boolean) -> Unit) {
        if (query.isBlank()) {
            onResult(false)
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                val addresses = geocoder.getFromLocationName(query, 1)
                val address = addresses?.firstOrNull()
                if (address != null) {
                    val dest = Destination(
                        name = query.replaceFirstChar { it.uppercase() },
                        latitude = address.latitude,
                        longitude = address.longitude,
                        address = address.getAddressLine(0) ?: query
                    )
                    withContext(Dispatchers.Main) {
                        _selectedDestination.value = dest
                        onResult(true)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        onResult(false)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onResult(false)
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun fetchLocation(context: Context) {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        val isGpsEnabled = locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true ||
                locationManager?.isProviderEnabled(LocationManager.NETWORK_PROVIDER) == true

        if (!isGpsEnabled) {
            _uiState.value = HomeUiState.GpsDisabled
            return
        }

        _uiState.value = HomeUiState.Loading

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        val cancellationTokenSource = CancellationTokenSource()

        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            cancellationTokenSource.token
        ).addOnSuccessListener { location: Location? ->
            if (location != null) {
                val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                val timeString = dateFormat.format(Date(location.time))
                val addressName = resolveAddress(context, location.latitude, location.longitude)

                _uiState.value = HomeUiState.Success(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    accuracy = location.accuracy,
                    timestamp = timeString,
                    address = addressName
                )
            } else {
                fusedLocationClient.lastLocation.addOnSuccessListener { lastLoc ->
                    if (lastLoc != null) {
                        val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                        val timeString = dateFormat.format(Date(lastLoc.time))
                        val addressName = resolveAddress(context, lastLoc.latitude, lastLoc.longitude)

                        _uiState.value = HomeUiState.Success(
                            latitude = lastLoc.latitude,
                            longitude = lastLoc.longitude,
                            accuracy = lastLoc.accuracy,
                            timestamp = timeString,
                            address = addressName
                        )
                    } else {
                        _uiState.value = HomeUiState.Error("Unable to retrieve location. Please try again.")
                    }
                }.addOnFailureListener { e ->
                    _uiState.value = HomeUiState.Error(e.localizedMessage ?: "Unknown location error")
                }
            }
        }.addOnFailureListener { e ->
            _uiState.value = HomeUiState.Error(e.localizedMessage ?: "Unknown location error")
        }
    }

    private fun resolveAddress(context: Context, lat: Double, lon: Double): String {
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(lat, lon, 1)
            val address = addresses?.firstOrNull()
            if (address != null) {
                val thoroughfare = address.thoroughfare ?: ""
                val locality = address.locality ?: address.subAdminArea ?: ""
                if (thoroughfare.isNotBlank() && locality.isNotBlank()) {
                    "$thoroughfare, $locality"
                } else {
                    address.getAddressLine(0) ?: "Current GPS Position"
                }
            } else {
                "Current GPS Position"
            }
        } catch (e: Exception) {
            "Current GPS Position"
        }
    }

    fun setPermissionDenied() {
        _uiState.value = HomeUiState.PermissionDenied
    }
}
