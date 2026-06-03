package com.example.ecomonitormobile.views

import android.os.Bundle
import android.view.ViewGroup
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ecomonitormobile.R
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ecomonitormobile.components.StationClusterItem
import com.example.ecomonitormobile.components.StationClusterRenderer
import com.example.ecomonitormobile.models.MonitoringStation.MonitoringStation
import com.example.ecomonitormobile.network.ApiClient
import com.example.ecomonitormobile.network.Repositories.MonitoringStationRepository
import com.example.ecomonitormobile.network.Repositories.StationDetailRepository
import com.example.ecomonitormobile.network.ViewModels.monitoringStation.MonitoringStationsUiState
import com.example.ecomonitormobile.network.ViewModels.alerts.AlertsViewModel
import com.example.ecomonitormobile.network.ViewModels.monitoringStation.MonitoringStationsViewModel
import com.example.ecomonitormobile.network.ViewModels.monitoringStation.MonitoringStationsViewModelFactory
import com.example.ecomonitormobile.network.ViewModels.station.StationDetailUiState
import com.example.ecomonitormobile.network.ViewModels.station.StationDetailViewModel
import com.example.ecomonitormobile.network.ViewModels.station.StationDetailViewModelFactory
import com.example.ecomonitormobile.views.station.StationInfoScreen
import com.example.ecomonitormobile.views.station.StationMarkerSheet
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng as GmsLatLng
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterManager

@Composable
fun HomeScreen(
    alertsViewModel: AlertsViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val repository = remember { MonitoringStationRepository(ApiClient.apiService) }
    val stationsViewModel: MonitoringStationsViewModel = viewModel(
        factory = MonitoringStationsViewModelFactory(repository)
    )
    val uiState by stationsViewModel.uiState.observeAsState(MonitoringStationsUiState.Loading)
    val alertStationIds by alertsViewModel.alertStationIds.observeAsState(emptySet())
    val focusStationId by alertsViewModel.focusStationId.observeAsState()

    var googleMap by remember { mutableStateOf<GoogleMap?>(null) }
    var clusterManager by remember { mutableStateOf<ClusterManager<StationClusterItem>?>(null) }
    var selectedStation by remember { mutableStateOf<MonitoringStation?>(null) }
    var showStationInfo by remember { mutableStateOf(false) }

    val mapView = remember { MapView(context) }
    val configuration = LocalConfiguration.current
    val stationStatusActive = stringResource(R.string.station_active)
    val stationStatusInactive = stringResource(R.string.station_inactive)
    val stationAlertSnippet = stringResource(R.string.station_alert_snippet)

    DisposableEffect(mapView, lifecycleOwner) {
        mapView.onCreate(Bundle())
        mapView.onResume()
        mapView.getMapAsync { gMap ->
            googleMap = gMap
            gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(50.450981, 30.508513), 10f))

            val cm = ClusterManager<StationClusterItem>(context, gMap)
            clusterManager = cm
            gMap.setOnMarkerClickListener(cm)
            gMap.setOnCameraIdleListener(cm)
            cm.renderer = StationClusterRenderer(context, gMap, cm)

            cm.setOnClusterItemClickListener { clusterItem ->
                selectedStation = clusterItem.getStation()
                true
            }
        }
        onDispose {
            mapView.onPause()
            mapView.onDestroy()
        }
    }

    LaunchedEffect(
        uiState,
        googleMap,
        clusterManager,
        configuration.locales,
        alertStationIds,
        stationStatusActive,
        stationStatusInactive,
        stationAlertSnippet
    ) {
        if (uiState is MonitoringStationsUiState.Success && googleMap != null && clusterManager != null) {
            val stations = (uiState as MonitoringStationsUiState.Success).stations
            clusterManager?.clearItems()
            clusterManager?.addItems(
                stations.map { station ->
                    val hasAlert = station.id in alertStationIds
                    val markerSnippet = buildString {
                        append(if (station.isActive) stationStatusActive else stationStatusInactive)
                        if (hasAlert) {
                            append("\n").append(stationAlertSnippet)
                        }
                        station.address?.takeIf { it.isNotBlank() }?.let { append("\n").append(it) }
                    }
                    StationClusterItem(
                        station = station,
                        hasAlert = hasAlert,
                        markerSnippet = markerSnippet
                    )
                }
            )
            clusterManager?.cluster()
        }
    }

    LaunchedEffect(focusStationId, uiState, googleMap) {
        val stationId = focusStationId ?: return@LaunchedEffect
        val stations = (uiState as? MonitoringStationsUiState.Success)?.stations ?: return@LaunchedEffect
        val station = stations.find { it.id == stationId } ?: return@LaunchedEffect
        selectedStation = station
        googleMap?.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
                GmsLatLng(station.latitude.toDouble(), station.longitude.toDouble()),
                14f
            )
        )
        alertsViewModel.clearFocusStation()
    }

    if (showStationInfo && selectedStation != null) {
        BackHandler { showStationInfo = false }
        StationInfoScreen(
            station = selectedStation!!,
            onBack = {
                showStationInfo = false
                selectedStation = null
            },
            modifier = modifier
        )
        return
    }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = {
                mapView.apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
                mapView
            },
            update = {}
        )

        when (uiState) {
            is MonitoringStationsUiState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp)
                )
            }
            is MonitoringStationsUiState.Error -> {
                Column(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = (uiState as MonitoringStationsUiState.Error).message,
                        color = MaterialTheme.colorScheme.error
                    )
                    TextButton(onClick = { stationsViewModel.loadStations() }) {
                        Text(stringResource(R.string.home_retry))
                    }
                }
            }
            else -> Unit
        }

        selectedStation?.let { station ->
            StationSheetWithData(
                station = station,
                onDismiss = { selectedStation = null },
                onOpenDetails = { showStationInfo = true }
            )
        }
    }
}

@Composable
private fun StationSheetWithData(
    station: MonitoringStation,
    onDismiss: () -> Unit,
    onOpenDetails: () -> Unit
) {
    val detailRepository = remember { StationDetailRepository(ApiClient.apiService) }
    val detailViewModel: StationDetailViewModel = viewModel(
        key = "station_${station.id}",
        factory = StationDetailViewModelFactory(detailRepository)
    )
    val detailState by detailViewModel.uiState.observeAsState(StationDetailUiState.Idle)

    LaunchedEffect(station.id) {
        detailViewModel.loadStationDetail(station.id, readingsLimit = 8)
    }

    StationMarkerSheet(
        station = station,
        detailState = detailState,
        onDismiss = {
            detailViewModel.reset()
            onDismiss()
        },
        onOpenDetails = onOpenDetails
    )
}
