package com.example.ecomonitormobile.network.ApiService

import com.example.ecomonitormobile.models.Login.LoginRequest
import com.example.ecomonitormobile.models.Login.LoginResponse
import com.example.ecomonitormobile.models.MonitoringStation.MonitoringStation
import com.example.ecomonitormobile.models.Profile.UpdateProfile
import com.example.ecomonitormobile.models.Settings.UpdateSettingsDto
import com.example.ecomonitormobile.models.Settings.UserSettings
import com.example.ecomonitormobile.models.Sensor.Sensor
import com.example.ecomonitormobile.models.Sensor.SensorReading
import com.example.ecomonitormobile.models.Notification.AppNotification
import com.example.ecomonitormobile.models.Notification.MarkNotificationsReadRequest
import com.example.ecomonitormobile.models.Notification.MarkNotificationsReadResponse

import retrofit2.Response
import retrofit2.http.Query
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.DELETE
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.Part
import retrofit2.http.Path

interface ApiService {
    //Auth
    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    @GET("auth/me")
    suspend fun getCurrentUser(
    ): Response<LoginResponse>

    //Monitoring station
    @GET("monitoring-station")
    suspend fun getMonitoringStations(
    ): Response<List<MonitoringStation>>

    @GET("sensors/station/{stationId}")
    suspend fun getSensorsByStation(
        @Path("stationId") stationId: Int
    ): Response<List<Sensor>>

    @GET("sensor-readings/latest")
    suspend fun getLatestReadings(
        @Query("stationId") stationId: Int? = null,
        @Query("sensorSerialNumber") sensorSerialNumber: String? = null,
        @Query("limit") limit: Int = 10
    ): Response<List<SensorReading>>

    //Profile
    @PATCH("user/profile")
    suspend fun updateProfile(@Body updateProfileDto: UpdateProfile): Response<Unit>

    //Settings
    @GET("settings")
    suspend fun getSettings(): Response<UserSettings>

    @PATCH("settings")
    suspend fun updateSettings(@Body updateSettingsDto: UpdateSettingsDto): Response<UserSettings>

    //Notifications
    @GET("notifications")
    suspend fun getNotifications(): Response<List<AppNotification>>

    @GET("notifications/{id}")
    suspend fun getNotificationById(@Path("id") id: Int): Response<AppNotification>

    @PATCH("notifications/read")
    suspend fun markNotificationsAsRead(
        @Body body: MarkNotificationsReadRequest
    ): Response<MarkNotificationsReadResponse>
}