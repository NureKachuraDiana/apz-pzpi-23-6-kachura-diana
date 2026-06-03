package com.example.ecomonitormobile.models.Settings

data class UpdateSettingsDto(
    val language: String? = null,
    val measurementUnit: String? = null,
    val notificationsEnabled: Boolean? = null,
    val darkModeEnabled: Boolean? = null,
    val emailNotifications: Boolean? = null,
    val pushNotifications: Boolean? = null,
    val smsNotifications: Boolean? = null
) {
    fun hasChanges(): Boolean =
        language != null ||
            measurementUnit != null ||
            notificationsEnabled != null ||
            darkModeEnabled != null ||
            emailNotifications != null ||
            pushNotifications != null ||
            smsNotifications != null
}
