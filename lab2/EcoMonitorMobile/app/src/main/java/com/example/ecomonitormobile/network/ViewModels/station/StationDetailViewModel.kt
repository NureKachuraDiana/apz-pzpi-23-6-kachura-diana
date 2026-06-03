package com.example.ecomonitormobile.network.ViewModels.station

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecomonitormobile.models.Sensor.Sensor
import com.example.ecomonitormobile.models.Sensor.SensorReading
import com.example.ecomonitormobile.network.Repositories.StationDetailRepository
import kotlinx.coroutines.launch

sealed class StationDetailUiState {
    object Idle : StationDetailUiState()
    object Loading : StationDetailUiState()
    data class Success(
        val sensors: List<Sensor>,
        val latestReadings: List<SensorReading>
    ) : StationDetailUiState() {
        val activeSensors: List<Sensor> get() = sensors.filter { it.isActive }
    }
    data class Error(val message: String) : StationDetailUiState()
}

class StationDetailViewModel(
    private val repository: StationDetailRepository
) : ViewModel() {

    private val _uiState = MutableLiveData<StationDetailUiState>(StationDetailUiState.Idle)
    val uiState: LiveData<StationDetailUiState> = _uiState

    private var loadedStationId: Int? = null
    private var loadedReadingsLimit: Int = 0

    fun loadStationDetail(stationId: Int, readingsLimit: Int = 50, forceRefresh: Boolean = false) {
        if (!forceRefresh &&
            loadedStationId == stationId &&
            loadedReadingsLimit >= readingsLimit &&
            _uiState.value is StationDetailUiState.Success
        ) {
            return
        }

        _uiState.value = StationDetailUiState.Loading
        viewModelScope.launch {
            repository.getStationDetail(stationId, readingsLimit)
                .onSuccess { data ->
                    loadedStationId = stationId
                    loadedReadingsLimit = readingsLimit
                    _uiState.value = StationDetailUiState.Success(
                        sensors = data.sensors,
                        latestReadings = sortReadings(data.latestReadings)
                    )
                }
                .onFailure { e ->
                    _uiState.value = StationDetailUiState.Error(
                        e.message ?: "Не вдалося завантажити дані станції"
                    )
                }
        }
    }

    fun reset() {
        loadedStationId = null
        loadedReadingsLimit = 0
        _uiState.value = StationDetailUiState.Idle
    }

    private fun sortReadings(readings: List<SensorReading>): List<SensorReading> =
        readings.sortedByDescending { it.timestamp }
}
