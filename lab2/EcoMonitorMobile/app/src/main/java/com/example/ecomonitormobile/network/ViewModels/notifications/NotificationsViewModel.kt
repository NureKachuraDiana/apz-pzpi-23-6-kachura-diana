package com.example.ecomonitormobile.network.ViewModels.notifications

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecomonitormobile.models.Notification.AppNotification
import com.example.ecomonitormobile.network.Repositories.NotificationsRepository
import kotlinx.coroutines.launch

sealed class NotificationsUiState {
    object Idle : NotificationsUiState()
    object Loading : NotificationsUiState()
    data class Success(
        val notifications: List<AppNotification>,
        val unreadCount: Int
    ) : NotificationsUiState()
    data class Error(val message: String) : NotificationsUiState()
}

enum class NotificationFilter { ALL, UNREAD, READ }

class NotificationsViewModel(
    private val repository: NotificationsRepository
) : ViewModel() {

    private val _uiState = MutableLiveData<NotificationsUiState>(NotificationsUiState.Idle)
    val uiState: LiveData<NotificationsUiState> = _uiState

    private var allNotifications: List<AppNotification> = emptyList()

    private val _currentFilter = MutableLiveData(NotificationFilter.ALL)
    val currentFilter: LiveData<NotificationFilter> = _currentFilter

    fun loadNotifications() {
        _uiState.value = NotificationsUiState.Loading
        viewModelScope.launch {
            repository.getNotifications()
                .onSuccess { list ->
                    allNotifications = list.sortedByDescending { it.createdAt }
                    publishFiltered()
                }
                .onFailure { e ->
                    _uiState.value = NotificationsUiState.Error(
                        e.message ?: "Failed to load notifications"
                    )
                }
        }
    }

    fun setFilter(filter: NotificationFilter) {
        _currentFilter.value = filter
        publishFiltered()
    }

    fun markAsRead(notificationIds: List<Int>? = null) {
        viewModelScope.launch {
            repository.markAsRead(notificationIds)
                .onSuccess { loadNotifications() }
                .onFailure { e ->
                    _uiState.value = NotificationsUiState.Error(
                        e.message ?: "Failed to mark as read"
                    )
                }
        }
    }

    fun markAllAsRead() {
        markAsRead(null)
    }

    fun markSingleAsRead(id: Int) {
        markAsRead(listOf(id))
    }

    private fun publishFiltered() {
        val filter = _currentFilter.value ?: NotificationFilter.ALL
        val filtered = when (filter) {
            NotificationFilter.ALL -> allNotifications
            NotificationFilter.UNREAD -> allNotifications.filter { !it.isRead }
            NotificationFilter.READ -> allNotifications.filter { it.isRead }
        }
        val unread = allNotifications.count { !it.isRead }
        _uiState.value = NotificationsUiState.Success(filtered, unread)
    }
}