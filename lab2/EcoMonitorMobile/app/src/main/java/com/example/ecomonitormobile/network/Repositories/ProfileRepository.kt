package com.example.ecomonitormobile.network.Repositories

import com.example.ecomonitormobile.models.Login.LoginResponse
import com.example.ecomonitormobile.models.Profile.UpdateProfile
import com.example.ecomonitormobile.network.ApiService.ApiService

class ProfileRepository(private val apiService: ApiService) {
    suspend fun updateProfile(updateProfileDto: UpdateProfile): Result<LoginResponse> {
        return try {
            val updateResponse = apiService.updateProfile(updateProfileDto)
            if (!updateResponse.isSuccessful) {
                val error = updateResponse.errorBody()?.string()
                return Result.failure(Exception("Update failed: ${updateResponse.code()} $error"))
            }

            val userResponse = apiService.getCurrentUser()
            if (userResponse.isSuccessful && userResponse.body() != null) {
                Result.success(userResponse.body()!!)
            } else {
                val error = userResponse.errorBody()?.string()
                Result.failure(Exception("Failed to fetch updated user: $error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}