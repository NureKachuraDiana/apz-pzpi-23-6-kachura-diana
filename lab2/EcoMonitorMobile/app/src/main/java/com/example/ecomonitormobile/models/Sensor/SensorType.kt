package com.example.ecomonitormobile.models.Sensor

enum class SensorType {
    TEMPERATURE,
    HUMIDITY,
    PRESSURE,
    AIR_QUALITY,
    PM25,
    PM10,
    CO2,
    NO2,
    O3,
    SO2,
    WIND_SPEED,
    WIND_DIRECTION,
    RAINFALL,
    UV_INDEX,
    NOISE,
    VOC,
    OTHER;

    companion object {
        fun fromApi(value: String?): SensorType {
            if (value.isNullOrBlank()) return OTHER
            return entries.find { it.name.equals(value, ignoreCase = true) }
                ?: runCatching { valueOf(value.uppercase()) }.getOrDefault(OTHER)
        }
    }
}
