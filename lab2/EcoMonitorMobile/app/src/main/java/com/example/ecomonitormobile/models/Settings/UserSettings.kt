package com.example.ecomonitormobile.models.Settings

data class UserSettings(
    val id: Int? = null,
    val userId: Int? = null,
    val language: String = "en",
    val measurementUnit: String = "metric",
    val notificationsEnabled: Boolean = true,
    val darkModeEnabled: Boolean = false,
    val emailNotifications: Boolean = true,
    val pushNotifications: Boolean = true,
    val smsNotifications: Boolean = false,
    val updatedAt: String? = null
)
