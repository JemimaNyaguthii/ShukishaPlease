package com.jem.shukishaplease.core.model

data class Destination(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val address: String = ""
)
