package com.jem.shukishaplease.core.model

data class Trip(
    val destination: Destination,
    val isActive: Boolean = false,
    val currentDistanceMeters: Float? = null
)
