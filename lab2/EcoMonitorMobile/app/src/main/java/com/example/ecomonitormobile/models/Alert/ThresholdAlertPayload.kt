package com.example.ecomonitormobile.models.Alert

data class ThresholdAlertPayload(
    val stationId: Int,
    val stationName: String,
    val sensor: SensorInfo,
    val reading: Double,
    val unit: String? = null,
    val threshold: ThresholdInfo,
    val title: String,
    val message: String,
    val timestamp: String
) {
    data class SensorInfo(
        val id: Int,
        val name: String,
        val type: String,
        val serialNumber: String
    )

    data class ThresholdInfo(
        val id: Int,
        val value: Double,
        val minValue: Double? = null,
        val maxValue: Double? = null,
        val severity: String
    )
}
