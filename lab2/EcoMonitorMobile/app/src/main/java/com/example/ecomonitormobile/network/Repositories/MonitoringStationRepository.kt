package com.example.ecomonitormobile.network.Repositories

import android.util.Log
import com.example.ecomonitormobile.models.MonitoringStation.MonitoringStation
import com.example.ecomonitormobile.network.ApiService.ApiService

class MonitoringStationRepository(private val apiService: ApiService) {

    suspend fun getMonitoringStations(): Result<List<MonitoringStation>> {
        return try {
            val response = apiService.getMonitoringStations()
            if (response.isSuccessful && response.body() != null) {
                Log.d("StationRepo", "Загружено станций: ${response.body()?.size}")
                Result.success(response.body()!!)
            } else {
                val error = response.errorBody()?.string()
                Log.e("StationRepo", "Ошибка загрузки: ${response.code()} $error")
                Result.failure(Exception("Ошибка загрузки: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("StationRepo", "Исключение: ${e.message}")
            Result.failure(e)
        }
    }
}