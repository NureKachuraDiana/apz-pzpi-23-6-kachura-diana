package com.example.ecomonitormobile.views.station

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ecomonitormobile.R
import com.example.ecomonitormobile.localization.localizedName
import com.example.ecomonitormobile.localization.localizedStringResource
import com.example.ecomonitormobile.models.Sensor.Sensor
import com.example.ecomonitormobile.models.Sensor.SensorReading
import com.example.ecomonitormobile.util.StationFormatters

private val ActiveGreen = Color(0xFF16A34A)
private val InactiveGray = Color(0xFF6B7280)
private val WarningAmber = Color(0xFFD97706)

@Composable
fun StationStatusChip(isActive: Boolean, modifier: Modifier = Modifier) {
    val bg = if (isActive) Color(0xFFDCFCE7) else Color(0xFFF3F4F6)
    val textColor = if (isActive) ActiveGreen else InactiveGray
    val label = if (isActive) {
        localizedStringResource(R.string.station_active)
    } else {
        localizedStringResource(R.string.station_inactive)
    }

    Surface(
        modifier = modifier,
        color = bg,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            color = textColor,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun InfoRow(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@Composable
fun SensorCard(sensor: Sensor, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Sensors,
                contentDescription = null,
                tint = if (sensor.isActive) ActiveGreen else InactiveGray,
                modifier = Modifier.size(28.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = sensor.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = sensor.sensorType().localizedName(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = localizedStringResource(R.string.sensor_serial, sensor.serialNumber),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
                sensor.model?.let {
                    Text(
                        text = localizedStringResource(R.string.sensor_model, it),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            StationStatusChip(isActive = sensor.isActive)
        }
    }
}

@Composable
fun ReadingCard(reading: SensorReading, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (reading.hasViolations()) {
                Color(0xFFFFF7ED)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            }
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = reading.sensor.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = reading.sensor.sensorType().localizedName(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = StationFormatters.formatReadingValue(reading),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (reading.hasViolations()) WarningAmber else ActiveGreen
                )
            }
            Text(
                text = StationFormatters.formatTimestamp(reading.timestamp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp)
            )
            reading.quality?.let { quality ->
                Text(
                    text = localizedStringResource(R.string.reading_quality, (quality * 100).toInt()),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            if (reading.hasViolations()) {
                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = WarningAmber,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = localizedStringResource(
                            R.string.reading_threshold_violations,
                            reading.thresholdViolations!!.size
                        ),
                        style = MaterialTheme.typography.labelMedium,
                        color = WarningAmber
                    )
                }
            }
        }
    }
}

@Composable
fun ReadingsList(
    readings: List<SensorReading>,
    emptyMessage: String,
    modifier: Modifier = Modifier
) {
    if (readings.isEmpty()) {
        Text(
            text = emptyMessage,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = modifier.padding(vertical = 8.dp)
        )
    } else {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            readings.forEach { reading ->
                ReadingCard(reading = reading)
            }
        }
    }
}
