package com.example.ecomonitormobile.views

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ecomonitormobile.R
import com.example.ecomonitormobile.network.ApiClient

import com.example.ecomonitormobile.network.Repositories.ProfileRepository
import com.example.ecomonitormobile.network.ViewModels.profile.ProfileUiState
import com.example.ecomonitormobile.network.ViewModels.profile.ProfileViewModel
import com.example.ecomonitormobile.network.ViewModels.profile.ProfileViewModelFactory

import com.example.ecomonitormobile.models.Login.LoginResponse
@Composable
fun ProfileScreen(
    user: LoginResponse,
    onLogout: () -> Unit,
    onProfileUpdated: (LoginResponse) -> Unit,
    onLanguageChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showSettings by remember { mutableStateOf(false) }

    if (showSettings) {
        SettingsScreen(
            onBack = { showSettings = false },
            onLanguageChanged = onLanguageChanged,
            modifier = modifier
        )
    } else {
        ProfileContent(
            user = user,
            onLogout = onLogout,
            onProfileUpdated = onProfileUpdated,
            onSettingsClick = { showSettings = true },
            modifier = modifier
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileContent(
    user: LoginResponse,
    onLogout: () -> Unit,
    onProfileUpdated: (LoginResponse) -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val repository = ProfileRepository(ApiClient.apiService)
    val viewModel: ProfileViewModel = viewModel(factory = ProfileViewModelFactory(repository))
    val uiState by viewModel.uiState.observeAsState(ProfileUiState.Idle)

    var firstName by remember { mutableStateOf(user.firstName ?: "") }
    var lastName by remember { mutableStateOf(user.lastName ?: "") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(uiState) {
        when (uiState) {
            is ProfileUiState.Success -> {
                val updatedUser = (uiState as ProfileUiState.Success).user
                onProfileUpdated(updatedUser)
                viewModel.resetState()
                errorMessage = null
            }
            is ProfileUiState.Error -> {
                errorMessage = (uiState as ProfileUiState.Error).message
            }
            else -> Unit
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.profile_title)) },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.profile_settings))
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Аватар (круглый с инициалами)
            Surface(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.primary,
                content = {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = getInitials(user.firstName, user.lastName),
                            fontSize = 32.sp,
                            color = Color.White
                        )
                    }
                }
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Email
            Text(
                text = user.email ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Поля ввода
            OutlinedTextField(
                value = firstName,
                onValueChange = { firstName = it },
                label = { Text(stringResource(R.string.profile_first_name)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = lastName,
                onValueChange = { lastName = it },
                label = { Text(stringResource(R.string.profile_last_name)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Дополнительная информация (только для чтения)
            Text(
                text = stringResource(R.string.profile_role, user.role ?: "-"),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = stringResource(R.string.profile_last_login, user.lastLogin ?: "-"),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Кнопка Save
            Button(
                onClick = {
                    errorMessage = null
                    viewModel.updateProfile(
                        user,
                        firstName.takeIf { it != user.firstName },
                        lastName.takeIf { it != user.lastName }
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState != ProfileUiState.Loading
            ) {
                if (uiState == ProfileUiState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Text(stringResource(R.string.profile_save))
                }
            }

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Кнопка Logout (красная)
            Button(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text(stringResource(R.string.profile_logout), color = Color.White)
            }
        }
    }
}

private fun getInitials(firstName: String?, lastName: String?): String {
    val first = firstName?.take(1)?.uppercase() ?: ""
    val last = lastName?.take(1)?.uppercase() ?: ""
    return if (first.isEmpty() && last.isEmpty()) "?" else "$first$last"
}
