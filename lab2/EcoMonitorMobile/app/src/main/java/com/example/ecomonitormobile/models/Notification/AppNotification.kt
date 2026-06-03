package com.example.ecomonitormobile.models.Notification

data class AppNotification(
    val id: Int,
    val userId: Int,
    val title: String,
    val message: String,
    val type: String,
    val isRead: Boolean,
    val priority: String? = null,
    val createdAt: String,
    val expiresAt: String? = null
)

data class MarkNotificationsReadRequest(
    val notificationIds: List<Int>? = null
)

data class MarkNotificationsReadResponse(
    val count: Int
)
