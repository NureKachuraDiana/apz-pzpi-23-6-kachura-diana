package com.example.ecomonitormobile.network.Repositories

import com.example.ecomonitormobile.models.Notification.AppNotification
import com.example.ecomonitormobile.models.Notification.MarkNotificationsReadRequest
import com.example.ecomonitormobile.models.Notification.MarkNotificationsReadResponse
import com.example.ecomonitormobile.network.ApiService.ApiService

class NotificationsRepository(private val apiService: ApiService) {

    suspend fun getNotifications(): Result<List<AppNotification>> = apiCall {
        apiService.getNotifications()
    }

    suspend fun getNotificationById(id: Int): Result<AppNotification> = apiCall {
        apiService.getNotificationById(id)
    }

    suspend fun markAsRead(notificationIds: List<Int>? = null): Result<MarkNotificationsReadResponse> =
        apiCall {
            apiService.markNotificationsAsRead(
                MarkNotificationsReadRequest(notificationIds = notificationIds)
            )
        }

    private suspend fun <T> apiCall(call: suspend () -> retrofit2.Response<T>): Result<T> {
        return try {
            val response = call()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val error = response.errorBody()?.string()
                Result.failure(Exception("Error ${response.code()}: ${error ?: "unknown"}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
