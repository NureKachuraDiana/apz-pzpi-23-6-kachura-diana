package com.example.ecomonitormobile

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ecomonitormobile.localization.AppLanguage
import com.example.ecomonitormobile.localization.LocalePreferences
import com.example.ecomonitormobile.localization.ProvideAppLocale
import com.example.ecomonitormobile.models.Login.LoginResponse
import com.example.ecomonitormobile.network.ApiClient
import com.example.ecomonitormobile.network.Repositories.AuthRepository
import com.example.ecomonitormobile.network.Repositories.SettingsRepository
import com.example.ecomonitormobile.network.ViewModels.alerts.AlertsViewModel
import com.example.ecomonitormobile.network.ViewModels.alerts.AlertsViewModelFactory
import com.example.ecomonitormobile.network.ViewModels.auth.AuthUiState
import com.example.ecomonitormobile.network.ViewModels.auth.AuthViewModel
import com.example.ecomonitormobile.network.ViewModels.auth.AuthViewModelFactory
import com.example.ecomonitormobile.ui.theme.EcoMonitorMobileTheme
import com.example.ecomonitormobile.util.AppNotificationHelper
import com.example.ecomonitormobile.views.FavoritesScreen
import com.example.ecomonitormobile.views.HomeScreen
import com.example.ecomonitormobile.views.LoginScreen
import com.example.ecomonitormobile.views.NotificationsScreen
import com.example.ecomonitormobile.views.ProfileScreen
import com.example.ecomonitormobile.views.ThresholdAlertDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    private val requestNotificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        ApiClient.initialize(applicationContext)
        val localePreferences = LocalePreferences(applicationContext)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !AppNotificationHelper.hasPostPermission(this)
        ) {
            requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        setContent {
            var languageCode by remember { mutableStateOf(localePreferences.get()) }

            val applyLanguage: (String) -> Unit = { code ->
                val normalized = AppLanguage.normalize(code)
                languageCode = normalized
                localePreferences.set(normalized)
            }

            EcoMonitorMobileTheme {
                ProvideAppLocale(languageCode = languageCode) {
                    val authViewModel: AuthViewModel = viewModel(
                        factory = AuthViewModelFactory(AuthRepository(ApiClient.apiService))
                    )
                    var isAuthenticated by remember { mutableStateOf(false) }
                    var currentUser by remember { mutableStateOf<LoginResponse?>(null) }

                    LaunchedEffect(Unit) {
                        authViewModel.checkSession()
                    }

                    val uiState by authViewModel.uiState.observeAsState()
                    LaunchedEffect(uiState) {
                        when (val state = uiState) {
                            is AuthUiState.Success -> {
                                currentUser = state.user
                                isAuthenticated = true
                            }
                            is AuthUiState.Unauthorized -> {
                                isAuthenticated = false
                                currentUser = null
                            }
                            else -> Unit
                        }
                    }

                    LaunchedEffect(isAuthenticated) {
                        if (isAuthenticated) {
                            withContext(Dispatchers.IO) {
                                SettingsRepository(ApiClient.apiService).getSettings()
                            }.onSuccess { settings ->
                                applyLanguage(settings.language)
                            }
                        }
                    }

                    if (isAuthenticated && currentUser != null) {
                        val alertsViewModel: AlertsViewModel = viewModel(
                            factory = AlertsViewModelFactory(application)
                        )

                        LaunchedEffect(Unit) {
                            alertsViewModel.connectSocket()
                        }

                        EcoMonitorMobileApp(
                            user = currentUser!!,
                            alertsViewModel = alertsViewModel,
                            onLogout = {
                                alertsViewModel.disconnectSocket()
                                authViewModel.logout()
                                isAuthenticated = false
                            },
                            onProfileUpdated = { updatedUser ->
                                currentUser = updatedUser
                            },
                            onLanguageChanged = applyLanguage
                        )
                    } else {
                        LoginScreen { user ->
                            currentUser = user
                            isAuthenticated = true
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EcoMonitorMobileApp(
    user: LoginResponse,
    alertsViewModel: AlertsViewModel,
    onLogout: () -> Unit,
    onProfileUpdated: (LoginResponse) -> Unit,
    onLanguageChanged: (String) -> Unit
) {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }
    val pendingAlert by alertsViewModel.pendingAlert.observeAsState()

    pendingAlert?.let { alert ->
        ThresholdAlertDialog(
            alert = alert,
            onDismiss = { alertsViewModel.dismissAlertDialog() },
            onViewStation = {
                currentDestination = AppDestinations.HOME
                alertsViewModel.focusStation(alert.stationId)
            }
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .height(110.dp)
                    .navigationBarsPadding(),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp
            ) {
                AppDestinations.entries.forEach { destination ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                painter = painterResource(destination.icon),
                                contentDescription = stringResource(destination.labelRes),
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = { Text(stringResource(destination.labelRes)) },
                        selected = destination == currentDestination,
                        onClick = { currentDestination = destination }
                    )
                }
            }
        }
    ) { innerPadding ->
        when (currentDestination) {
            AppDestinations.HOME -> HomeScreen(
                alertsViewModel = alertsViewModel,
                modifier = Modifier.padding(innerPadding)
            )
            AppDestinations.FAVORITES -> FavoritesScreen(modifier = Modifier.padding(innerPadding))
            AppDestinations.PROFILE -> ProfileScreen(
                user = user,
                onLogout = onLogout,
                modifier = Modifier.padding(innerPadding),
                onProfileUpdated = onProfileUpdated,
                onLanguageChanged = onLanguageChanged
            )
            AppDestinations.NOTIFICATIONS -> NotificationsScreen(
                alertsViewModel = alertsViewModel,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

enum class AppDestinations(
    val labelRes: Int,
    val icon: Int,
) {
    HOME(R.string.nav_home, R.drawable.ic_home),
    FAVORITES(R.string.nav_favorites, R.drawable.ic_favorite),
    PROFILE(R.string.nav_profile, R.drawable.ic_account_box),
    NOTIFICATIONS(R.string.nav_notifications, R.drawable.ic_notifications),
}
