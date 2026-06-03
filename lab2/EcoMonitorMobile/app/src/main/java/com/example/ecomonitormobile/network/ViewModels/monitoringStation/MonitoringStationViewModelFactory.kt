package com.example.ecomonitormobile.network.ViewModels.monitoringStation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.ecomonitormobile.network.Repositories.MonitoringStationRepository

class MonitoringStationsViewModelFactory(
    private val repository: MonitoringStationRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MonitoringStationsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MonitoringStationsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}