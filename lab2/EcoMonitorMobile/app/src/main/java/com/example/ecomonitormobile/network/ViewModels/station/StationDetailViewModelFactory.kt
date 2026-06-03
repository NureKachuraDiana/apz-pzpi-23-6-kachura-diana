package com.example.ecomonitormobile.network.ViewModels.station

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.ecomonitormobile.network.Repositories.StationDetailRepository

class StationDetailViewModelFactory(
    private val repository: StationDetailRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StationDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StationDetailViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
