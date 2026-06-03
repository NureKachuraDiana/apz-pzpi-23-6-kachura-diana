package com.example.ecomonitormobile.network.ViewModels.alerts

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.ecomonitormobile.models.Alert.ThresholdAlertPayload
import com.example.ecomonitormobile.network.ApiClient
import com.example.ecomonitormobile.network.socket.EcoMonitorSocketClient
import com.example.ecomonitormobile.util.AppNotificationHelper

class AlertsViewModel(application: Application) : AndroidViewModel(application) {

    private val _alertStationIds = MutableLiveData<Set<Int>>(emptySet())
    val alertStationIds: LiveData<Set<Int>> = _alertStationIds

    private val _pendingAlert = MutableLiveData<ThresholdAlertPayload?>(null)
    val pendingAlert: LiveData<ThresholdAlertPayload?> = _pendingAlert

    private val _socketConnected = MutableLiveData(false)
    val socketConnected: LiveData<Boolean> = _socketConnected

    private val _alertEventVersion = MutableLiveData(0)
    val alertEventVersion: LiveData<Int> = _alertEventVersion

    private val _focusStationId = MutableLiveData<Int?>(null)
    val focusStationId: LiveData<Int?> = _focusStationId

    private var socketClient: EcoMonitorSocketClient? = null

    fun connectSocket() {
        if (socketClient != null) return

        val context = getApplication<Application>()
        AppNotificationHelper.createChannel(context)

        socketClient = EcoMonitorSocketClient(
            serverBaseUrl = ApiClient.BASE_URL.trimEnd('/'),
            onThresholdAlert = { alert -> handleThresholdAlert(alert) },
            onConnectionChange = { connected -> _socketConnected.postValue(connected) }
        ).also { it.connect() }
    }

    fun disconnectSocket() {
        socketClient?.disconnect()
        socketClient = null
        _socketConnected.value = false
    }

    fun dismissAlertDialog() {
        _pendingAlert.value = null
    }

    fun clearStationAlert(stationId: Int) {
        _alertStationIds.value = _alertStationIds.value.orEmpty() - stationId
    }

    fun focusStation(stationId: Int) {
        _focusStationId.value = stationId
        dismissAlertDialog()
    }

    fun clearFocusStation() {
        _focusStationId.value = null
    }

    private fun handleThresholdAlert(alert: ThresholdAlertPayload) {
        val updatedIds = _alertStationIds.value.orEmpty() + alert.stationId
        _alertStationIds.postValue(updatedIds)
        _pendingAlert.postValue(alert)
        _alertEventVersion.postValue((_alertEventVersion.value ?: 0) + 1)

        AppNotificationHelper.showThresholdAlert(getApplication(), alert)
    }

    override fun onCleared() {
        disconnectSocket()
        super.onCleared()
    }
}
