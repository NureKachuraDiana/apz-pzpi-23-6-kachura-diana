package com.example.ecomonitormobile.components

import com.example.ecomonitormobile.models.MonitoringStation.MonitoringStation
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem

class StationClusterItem(
    private val station: MonitoringStation,
    private val hasAlert: Boolean = false,
    private val markerSnippet: String = ""
) : ClusterItem {
    override fun getPosition(): LatLng = LatLng(station.latitude.toDouble(), station.longitude.toDouble())
    override fun getTitle(): String = station.name
    override fun getSnippet(): String = markerSnippet
    override fun getZIndex(): Float? = null

    fun getStation(): MonitoringStation = station

    fun hasAlert(): Boolean = hasAlert
}