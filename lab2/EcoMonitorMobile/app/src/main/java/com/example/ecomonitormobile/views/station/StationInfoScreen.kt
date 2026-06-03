package com.example.ecomonitormobile.views.station

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ecomonitormobile.R
import com.example.ecomonitormobile.localization.LocalizedContent
import com.example.ecomonitormobile.models.MonitoringStation.MonitoringStation
import com.example.ecomonitormobile.models.Sensor.Sensor
import com.example.ecomonitormobile.models.Sensor.SensorReading
import com.example.ecomonitormobile.network.ApiClient
import com.example.ecomonitormobile.network.Repositories.StationDetailRepository
import com.example.ecomonitormobile.network.ViewModels.station.StationDetailUiState
import com.example.ecomonitormobile.network.ViewModels.station.StationDetailViewModel
import com.example.ecomonitormobile.network.ViewModels.station.StationDetailViewModelFactory
import com.example.ecomonitormobile.util.StationFormatters

@Composable
private fun stationTabTitles(): List<String> = listOf(
    stringResource(R.string.station_tab_info),
    stringResource(R.string.station_tab_sensors),
    stringResource(R.string.station_tab_readings)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StationInfoScreen(
    station: MonitoringStation,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val repository = remember { StationDetailRepository(ApiClient.apiService) }
    val viewModel: StationDetailViewModel = viewModel(
        key = "station_${station.id}",
        factory = StationDetailViewModelFactory(repository)
    )
    val detailState by viewModel.uiState.observeAsState(StationDetailUiState.Idle)
    var selectedTab by remember { mutableIntStateOf(0) }

    LaunchedEffect(station.id) {
        viewModel.loadStationDetail(station.id, readingsLimit = 50, forceRefresh = true)
    }

    BackHandler(onBack = onBack)

    LocalizedContent {
        StationInfoScaffold(
            station = station,
            detailState = detailState,
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it },
            onBack = onBack,
            onRetry = { viewModel.loadStationDetail(station.id, readingsLimit = 50, forceRefresh = true) },
            modifier = modifier
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StationInfoScaffold(
    station: MonitoringStation,
    detailState: StationDetailUiState,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tabs = stationTabTitles()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = station.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1
                        )
                        Text(
                            text = stringResource(R.string.station_info_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                actions = {
                    StationStatusChip(isActive = station.isActive, modifier = Modifier.padding(end = 12.dp))
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { onTabSelected(index) },
                        text = { Text(title) }
                    )
                }
            }

            when (val state = detailState) {
                is StationDetailUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is StationDetailUiState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = resolveStationInfoError(state.message),
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            TextButton(
                                onClick = onRetry,
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
                                Text(stringResource(R.string.settings_retry))
                            }
                        }
                    }
                }
                is StationDetailUiState.Success -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        when (selectedTab) {
                            0 -> StationInfoTab(station = station)
                            1 -> StationSensorsTab(
                                sensors = state.activeSensors,
                                totalCount = state.sensors.size
                            )
                            2 -> StationReadingsTab(readings = state.latestReadings)
                        }
                    }
                }
                else -> Unit
            }
        }
    }
}

@Composable
private fun StationInfoTab(station: MonitoringStation) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            InfoRow(label = stringResource(R.string.station_label_name), value = station.name)
            InfoRow(
                label = stringResource(R.string.station_label_status),
                value = if (station.isActive) {
                    stringResource(R.string.station_active)
                } else {
                    stringResource(R.string.station_inactive)
                }
            )
            station.address?.let { InfoRow(label = stringResource(R.string.station_label_address), value = it) }
            station.description?.takeIf { it.isNotBlank() }?.let {
                InfoRow(label = stringResource(R.string.station_label_description), value = it)
            }
            station.altitude?.let {
                InfoRow(
                    label = stringResource(R.string.station_label_altitude),
                    value = stringResource(R.string.station_altitude_value, it.toString())
                )
            }
            InfoRow(
                label = stringResource(R.string.station_label_coordinates),
                value = StationFormatters.formatCoordinates(station.latitude, station.longitude)
            )
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Text(
            text = stringResource(R.string.station_readonly_notice),
            modifier = Modifier.padding(14.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun StationSensorsTab(sensors: List<Sensor>, totalCount: Int) {
    Text(
        text = stringResource(R.string.station_active_sensors, sensors.size, totalCount),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    if (sensors.isEmpty()) {
        Text(
            text = stringResource(R.string.station_no_active_sensors),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 8.dp)
        )
    } else {
        sensors.forEach { sensor ->
            SensorCard(sensor = sensor)
        }
    }
}

@Composable
private fun StationReadingsTab(readings: List<SensorReading>) {
    Text(
        text = stringResource(R.string.station_latest_readings_count, readings.size),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    ReadingsList(
        readings = readings,
        emptyMessage = stringResource(R.string.station_no_readings_tab)
    )
}

@Composable
private fun resolveStationInfoError(message: String?): String = when {
    message == null -> stringResource(R.string.error_station_detail)
    message.contains("load", ignoreCase = true) ||
        message.contains("завантаж", ignoreCase = true) -> stringResource(R.string.error_station_detail)
    else -> message
}
