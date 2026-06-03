package com.example.ecomonitormobile.views

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.example.ecomonitormobile.R
import com.example.ecomonitormobile.models.Alert.ThresholdAlertPayload
import com.example.ecomonitormobile.util.StationFormatters

private val AlertRed = Color(0xFFDC2626)

@Composable
fun ThresholdAlertDialog(
    alert: ThresholdAlertPayload,
    onDismiss: () -> Unit,
    onViewStation: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                tint = AlertRed
            )
        },
        title = {
            Text(
                text = alert.title,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = buildString {
                    appendLine(alert.message)
                    appendLine()
                    append("${alert.stationName} · ${alert.sensor.name}")
                    appendLine()
                    val unit = alert.unit?.let { " $it" }.orEmpty()
                    append("${stringResource(R.string.alert_reading_label)}: ${alert.reading}$unit")
                    appendLine()
                    append("${stringResource(R.string.alert_severity_label)}: ${alert.threshold.severity}")
                    appendLine()
                    append(StationFormatters.formatTimestamp(alert.timestamp))
                },
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            TextButton(onClick = onViewStation) {
                Text(stringResource(R.string.alert_view_station))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.alert_dismiss))
            }
        },
        containerColor = MaterialTheme.colorScheme.errorContainer,
        titleContentColor = MaterialTheme.colorScheme.onErrorContainer,
        textContentColor = MaterialTheme.colorScheme.onErrorContainer,
        iconContentColor = AlertRed
    )
}
