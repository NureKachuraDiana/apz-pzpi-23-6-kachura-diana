package com.example.ecomonitormobile.models.MonitoringStation

data class MonitoringStation(
    val id: Int,
    val name: String,
    val description: String?,
    val latitude: Float,
    val longitude: Float,
    val altitude: Float?,
    val address: String?,
    val isActive: Boolean
)