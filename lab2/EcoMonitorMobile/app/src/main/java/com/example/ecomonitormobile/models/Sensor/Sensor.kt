package com.example.ecomonitormobile.models.Sensor

import com.google.gson.annotations.SerializedName

data class Sensor(
    val id: Int,
    val stationId: Int,
    val type: String,
    val name: String,
    val serialNumber: String,
    val model: String? = null,
    val isActive: Boolean = true,
    val calibrationDate: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
) {
    fun sensorType(): SensorType = SensorType.fromApi(type)
}

data class StationRef(
    val id: Int,
    val name: String
)

data class SensorRef(
    val id: Int,
    val serialNumber: String,
    val type: String,
    val name: String,
    val station: StationRef
) {
    fun sensorType(): SensorType = SensorType.fromApi(type)
}

data class ThresholdViolation(
    val thresholdId: Int,
    val severity: String,
    val minValue: Double? = null,
    val maxValue: Double? = null,
    val description: String? = null,
    val actualValue: Double,
    val sensorType: String
)

data class SensorReading(
    val id: Int,
    val sensorId: Int,
    val value: Double,
    val unit: String? = null,
    val timestamp: String,
    val quality: Double? = null,
    val sensor: SensorRef,
    @SerializedName("thresholdViolations")
    val thresholdViolations: List<ThresholdViolation>? = null
) {
    fun hasViolations(): Boolean = !thresholdViolations.isNullOrEmpty()
}
