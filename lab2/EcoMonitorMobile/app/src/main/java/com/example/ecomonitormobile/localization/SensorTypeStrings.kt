package com.example.ecomonitormobile.localization

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.ecomonitormobile.R
import com.example.ecomonitormobile.models.Sensor.SensorType

@StringRes
fun SensorType.labelRes(): Int = when (this) {
    SensorType.TEMPERATURE -> R.string.sensor_type_temperature
    SensorType.HUMIDITY -> R.string.sensor_type_humidity
    SensorType.PRESSURE -> R.string.sensor_type_pressure
    SensorType.AIR_QUALITY -> R.string.sensor_type_air_quality
    SensorType.PM25 -> R.string.sensor_type_pm25
    SensorType.PM10 -> R.string.sensor_type_pm10
    SensorType.CO2 -> R.string.sensor_type_co2
    SensorType.NO2 -> R.string.sensor_type_no2
    SensorType.O3 -> R.string.sensor_type_o3
    SensorType.SO2 -> R.string.sensor_type_so2
    SensorType.WIND_SPEED -> R.string.sensor_type_wind_speed
    SensorType.WIND_DIRECTION -> R.string.sensor_type_wind_direction
    SensorType.RAINFALL -> R.string.sensor_type_rainfall
    SensorType.UV_INDEX -> R.string.sensor_type_uv_index
    SensorType.NOISE -> R.string.sensor_type_noise
    SensorType.VOC -> R.string.sensor_type_voc
    SensorType.OTHER -> R.string.sensor_type_other
}

@Composable
fun SensorType.localizedName(): String = stringResource(labelRes())
