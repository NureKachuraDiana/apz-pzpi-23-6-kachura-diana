package com.example.ecomonitormobile.network.Repositories

import com.example.ecomonitormobile.models.Settings.UpdateSettingsDto
import com.example.ecomonitormobile.models.Settings.UserSettings
import com.example.ecomonitormobile.network.ApiService.ApiService

class SettingsRepository(private val apiService: ApiService) {
    suspend fun getSettings(): Result<UserSettings> {
        return try {
            val response = apiService.getSettings()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val error = response.errorBody()?.string()
                Result.failure(Exception("Failed to load settings: ${response.code()} $error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateSettings(updateSettingsDto: UpdateSettingsDto): Result<UserSettings> {
        return try {
            val response = apiService.updateSettings(updateSettingsDto)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val error = response.errorBody()?.string()
                Result.failure(Exception("Failed to update settings: ${response.code()} $error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
