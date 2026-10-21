package com.jem.shukishaplease.features.home

sealed interface HomeUiState {
    object Idle : HomeUiState
    object Loading : HomeUiState
    data class Success(
        val latitude: Double,
        val longitude: Double,
        val accuracy: Float,
        val timestamp: String,
        val address: String = "Current Location"
    ) : HomeUiState
    data class Error(val message: String) : HomeUiState
    object PermissionDenied : HomeUiState
    object GpsDisabled : HomeUiState
}
