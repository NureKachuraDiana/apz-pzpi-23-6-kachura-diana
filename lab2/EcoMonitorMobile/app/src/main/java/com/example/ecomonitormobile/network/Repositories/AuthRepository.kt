package com.example.ecomonitormobile.network.Repositories

import android.util.Log
import com.example.ecomonitormobile.models.Login.LoginRequest
import com.example.ecomonitormobile.models.Login.LoginResponse
import com.example.ecomonitormobile.network.ApiService.ApiService

class AuthRepository(private val apiService: ApiService) {
    suspend fun login(request: LoginRequest): Result<LoginResponse> {
        return try {
            Log.d("AuthRepo", "Login request: $request")
            val response = apiService.login(request)
            Log.d("AuthRepo", "Response code: ${response.code()}")
            if (response.isSuccessful && response.body() != null) {
                Log.d("AuthRepo", "Success: ${response.body()}")
                Result.success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("AuthRepo", "Error code ${response.code()}: $errorBody")
                Result.failure(Exception("Login failed: ${response.code()} - $errorBody"))
            }
        } catch (e: Exception) {
            Log.e("AuthRepo", "Exception: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun getCurrentUser(): Result<LoginResponse> {
        return try {
            val response = apiService.getCurrentUser()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Session expired or invalid"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}