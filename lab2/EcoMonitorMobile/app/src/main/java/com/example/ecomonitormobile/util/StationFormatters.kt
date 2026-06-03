package com.example.ecomonitormobile.util

import com.example.ecomonitormobile.models.Sensor.SensorReading
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

object StationFormatters {
    private val inputFormats = listOf(
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") },
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") },
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.US),
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US)
    )
    private val displayFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale("uk"))

    fun formatTimestamp(iso: String?): String {
        if (iso.isNullOrBlank()) return "—"
        for (format in inputFormats) {
            try {
                val date = format.parse(iso.trim())
                if (date != null) {
                    return displayFormat.format(date)
                }
            } catch (_: Exception) {
                // try next format
            }
        }
        return iso
    }

    fun formatReadingValue(reading: SensorReading): String {
        val unit = reading.unit?.trim().orEmpty()
        val valueText = if (reading.value % 1.0 == 0.0) {
            reading.value.toInt().toString()
        } else {
            String.format(Locale.US, "%.2f", reading.value)
        }
        return if (unit.isNotEmpty()) "$valueText $unit" else valueText
    }

    fun formatCoordinates(lat: Float, lng: Float): String =
        String.format(Locale.US, "%.5f, %.5f", lat, lng)

    fun severityLabelUk(severity: String): String = when (severity.uppercase()) {
        "CRITICAL" -> "Критичний"
        "HIGH" -> "Високий"
        "MEDIUM" -> "Середній"
        "LOW" -> "Низький"
        else -> severity
    }
}
