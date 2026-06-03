package com.example.ecomonitormobile.network.ViewModels.monitoringStation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecomonitormobile.models.MonitoringStation.MonitoringStation
import com.example.ecomonitormobile.network.Repositories.MonitoringStationRepository
import kotlinx.coroutines.launch

sealed class MonitoringStationsUiState {
    object Loading : MonitoringStationsUiState()
    data class Success(val stations: List<MonitoringStation>) : MonitoringStationsUiState()
    data class Error(val message: String) : MonitoringStationsUiState()
}

class MonitoringStationsViewModel(
    private val repository: MonitoringStationRepository
) : ViewModel() {

    private val _uiState = MutableLiveData<MonitoringStationsUiState>()
    val uiState: LiveData<MonitoringStationsUiState> = _uiState

    init {
        loadStations()
    }

    fun loadStations() {
        _uiState.value = MonitoringStationsUiState.Loading
        viewModelScope.launch {
            val result = repository.getMonitoringStations()
            result.onSuccess { stations ->
                _uiState.value = MonitoringStationsUiState.Success(stations)
            }.onFailure { exception ->
                _uiState.value = MonitoringStationsUiState.Error(exception.message ?: "Неизвестная ошибка")
            }
        }
    }
}