package com.example.ecomonitormobile.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ecomonitormobile.R
import com.example.ecomonitormobile.models.Notification.AppNotification
import com.example.ecomonitormobile.network.ApiClient
import com.example.ecomonitormobile.network.Repositories.NotificationsRepository
import com.example.ecomonitormobile.network.ViewModels.alerts.AlertsViewModel
import com.example.ecomonitormobile.network.ViewModels.notifications.NotificationFilter
import com.example.ecomonitormobile.network.ViewModels.notifications.NotificationsUiState
import com.example.ecomonitormobile.network.ViewModels.notifications.NotificationsViewModel
import com.example.ecomonitormobile.network.ViewModels.notifications.NotificationsViewModelFactory
import com.example.ecomonitormobile.util.StationFormatters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    alertsViewModel: AlertsViewModel,
    modifier: Modifier = Modifier
) {
    val repository = remember { NotificationsRepository(ApiClient.apiService) }
    val viewModel: NotificationsViewModel = viewModel(
        factory = NotificationsViewModelFactory(repository)
    )
    val uiState by viewModel.uiState.observeAsState(NotificationsUiState.Idle)
    val currentFilter by viewModel.currentFilter.observeAsState(NotificationFilter.ALL)
    val alertEventVersion by alertsViewModel.alertEventVersion.observeAsState(0)

    LaunchedEffect(Unit) {
        viewModel.loadNotifications()
    }

    LaunchedEffect(alertEventVersion) {
        if (alertEventVersion > 0) {
            viewModel.loadNotifications()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.notifications_title)) },
                actions = {
                    val success = uiState as? NotificationsUiState.Success
                    if (success != null && success.unreadCount > 0) {
                        IconButton(onClick = { viewModel.markAllAsRead() }) {
                            Icon(
                                Icons.Default.DoneAll,
                                contentDescription = stringResource(R.string.notifications_mark_all_read)
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            NotificationFilterRow(
                selectedFilter = currentFilter,
                onFilterSelected = { viewModel.setFilter(it) }
            )

            when (val state = uiState) {
                is NotificationsUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is NotificationsUiState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = state.message,
                                color = MaterialTheme.colorScheme.error
                            )
                            TextButton(onClick = { viewModel.loadNotifications() }) {
                                Text(stringResource(R.string.settings_retry))
                            }
                        }
                    }
                }
                is NotificationsUiState.Success -> {
                    if (state.notifications.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.notifications_empty),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(state.notifications, key = { it.id }) { notification ->
                                NotificationCard(
                                    notification = notification,
                                    onMarkRead = { viewModel.markSingleAsRead(notification.id) }
                                )
                            }
                        }
                    }
                }
                else -> Unit
            }
        }
    }
}

@Composable
private fun NotificationFilterRow(
    selectedFilter: NotificationFilter,
    onFilterSelected: (NotificationFilter) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = selectedFilter == NotificationFilter.ALL,
            onClick = { onFilterSelected(NotificationFilter.ALL) },
            label = { Text(stringResource(R.string.notifications_filter_all)) }
        )
        FilterChip(
            selected = selectedFilter == NotificationFilter.UNREAD,
            onClick = { onFilterSelected(NotificationFilter.UNREAD) },
            label = { Text(stringResource(R.string.notifications_filter_unread)) }
        )
        FilterChip(
            selected = selectedFilter == NotificationFilter.READ,
            onClick = { onFilterSelected(NotificationFilter.READ) },
            label = { Text(stringResource(R.string.notifications_filter_read)) }
        )
    }
}

@Composable
private fun NotificationCard(
    notification: AppNotification,
    onMarkRead: () -> Unit
) {
    val isUnread = !notification.isRead
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnread) {
                Color(0xFFFFF7ED)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            }
        ),
        onClick = {
            if (isUnread) onMarkRead()
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Default.Notifications,
                contentDescription = null,
                tint = if (isUnread) Color(0xFFD97706) else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notification.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (isUnread) FontWeight.Bold else FontWeight.Normal
                    )
                    if (isUnread) {
                        Surface(
                            color = Color(0xFFDC2626),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                text = stringResource(R.string.notifications_unread_badge),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White
                            )
                        }
                    }
                }
                Text(
                    text = notification.message,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Text(
                    text = StationFormatters.formatTimestamp(notification.createdAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }
    }
}
