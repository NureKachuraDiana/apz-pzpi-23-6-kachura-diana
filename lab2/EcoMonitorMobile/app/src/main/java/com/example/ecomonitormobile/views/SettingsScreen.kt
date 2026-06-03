package com.example.ecomonitormobile.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.livedata.observeAsState
import com.example.ecomonitormobile.R
import com.example.ecomonitormobile.localization.AppLanguage
import com.example.ecomonitormobile.localization.LocalePreferences
import com.example.ecomonitormobile.models.Settings.UserSettings
import com.example.ecomonitormobile.network.ApiClient
import com.example.ecomonitormobile.network.Repositories.SettingsRepository
import com.example.ecomonitormobile.network.ViewModels.settings.SettingsUiState
import com.example.ecomonitormobile.network.ViewModels.settings.SettingsViewModel
import com.example.ecomonitormobile.network.ViewModels.settings.SettingsViewModelFactory
import kotlinx.coroutines.delay

private val SaveGreen = Color(0xFF16A34A)
private val SaveGreenDark = Color(0xFF15803D)

private data class SelectOption(val value: String, val label: String)

@Composable
private fun rememberLanguageOptions(): List<SelectOption> = listOf(
    SelectOption(AppLanguage.UK, stringResource(R.string.settings_language_uk)),
    SelectOption(AppLanguage.EN, stringResource(R.string.settings_language_en))
)

@Composable
private fun rememberMeasurementOptions(): List<SelectOption> = listOf(
    SelectOption("metric", stringResource(R.string.settings_unit_metric)),
    SelectOption("imperial", stringResource(R.string.settings_unit_imperial))
)

@Composable
private fun resolveSettingsError(message: String?): String = when {
    message == null -> stringResource(R.string.error_unknown)
    message.contains("No changes", ignoreCase = true) -> stringResource(R.string.settings_error_no_changes)
    message.contains("load", ignoreCase = true) -> stringResource(R.string.settings_error_load)
    message.contains("update", ignoreCase = true) -> stringResource(R.string.settings_error_update)
    else -> message
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onLanguageChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val localePreferences = remember(context) { LocalePreferences(context) }
    var languageAtOpen by remember { mutableStateOf(localePreferences.get()) }
    val repository = remember { SettingsRepository(ApiClient.apiService) }
    val viewModel: SettingsViewModel = viewModel(factory = SettingsViewModelFactory(repository))
    val uiState by viewModel.uiState.observeAsState(SettingsUiState.Idle)

    var formData by remember { mutableStateOf<UserSettings?>(null) }
    var initialData by remember { mutableStateOf<UserSettings?>(null) }
    var submitError by remember { mutableStateOf<String?>(null) }
    var showSuccess by remember { mutableStateOf(false) }
    val formMessage by viewModel.formMessage.observeAsState()

    LaunchedEffect(Unit) {
        viewModel.loadSettings()
    }

    LaunchedEffect(formMessage) {
        submitError = formMessage
    }

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is SettingsUiState.Loaded -> {
                val isInitialLoad = formData == null
                formData = state.settings
                initialData = state.settings
                submitError = null
                viewModel.clearFormMessage()
                languageAtOpen = AppLanguage.normalize(state.settings.language)
                if (isInitialLoad) {
                    onLanguageChanged(state.settings.language)
                }
            }
            is SettingsUiState.SaveSuccess -> {
                formData = state.settings
                initialData = state.settings
                submitError = null
                viewModel.clearFormMessage()
                showSuccess = true
                languageAtOpen = AppLanguage.normalize(state.settings.language)
                onLanguageChanged(state.settings.language)
                viewModel.resetToLoaded(state.settings)
            }
            is SettingsUiState.Error -> {
                if (formData != null) {
                    submitError = state.message
                }
            }
            else -> Unit
        }
    }

    LaunchedEffect(showSuccess) {
        if (showSuccess) {
            delay(3000)
            showSuccess = false
        }
    }

    val handleBack = {
        onLanguageChanged(languageAtOpen)
        onBack()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = handleBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        when {
            uiState is SettingsUiState.Loading && formData == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.settings_loading),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            uiState is SettingsUiState.Error && formData == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = resolveSettingsError((uiState as SettingsUiState.Error).message),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadSettings(forceRefresh = true) }) {
                            Text(stringResource(R.string.settings_retry))
                        }
                    }
                }
            }
            formData != null && initialData != null -> {
                val current = formData!!
                val saved = initialData!!
                val isSaving = uiState is SettingsUiState.Saving
                val hasChanges = current != saved

                SettingsFormContent(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 16.dp),
                    formData = current,
                    isSaving = isSaving,
                    hasChanges = hasChanges,
                    submitError = submitError?.let { resolveSettingsError(it) },
                    showSuccess = showSuccess,
                    onFormChange = { updated ->
                        if (updated.language != formData?.language) {
                            onLanguageChanged(updated.language)
                        }
                        formData = updated
                        showSuccess = false
                        submitError = null
                        viewModel.clearFormMessage()
                    },
                    onSave = {
                        submitError = null
                        showSuccess = false
                        viewModel.updateSettings(current, saved)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsFormContent(
    modifier: Modifier,
    formData: UserSettings,
    isSaving: Boolean,
    hasChanges: Boolean,
    submitError: String?,
    showSuccess: Boolean,
    onFormChange: (UserSettings) -> Unit,
    onSave: () -> Unit
) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        val languageOptions = rememberLanguageOptions()
        val measurementUnitOptions = rememberMeasurementOptions()

        Text(
            text = stringResource(R.string.settings_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (submitError != null) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.errorContainer,
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    text = submitError,
                    modifier = Modifier.padding(12.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        if (showSuccess) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFFDCFCE7),
                shape = MaterialTheme.shapes.medium
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = SaveGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = stringResource(R.string.settings_saved),
                        color = SaveGreenDark,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SettingsDropdown(
                    label = stringResource(R.string.settings_language),
                    description = stringResource(R.string.settings_language_description),
                    icon = Icons.Default.Language,
                    options = languageOptions,
                    selectedValue = formData.language,
                    enabled = !isSaving,
                    onValueChange = { onFormChange(formData.copy(language = it)) }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                SettingsDropdown(
                    label = stringResource(R.string.settings_measurement_unit),
                    description = stringResource(R.string.settings_measurement_description),
                    icon = Icons.Default.Straighten,
                    options = measurementUnitOptions,
                    selectedValue = formData.measurementUnit,
                    enabled = !isSaving,
                    onValueChange = { onFormChange(formData.copy(measurementUnit = it)) }
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                SettingsSwitchRow(
                    label = stringResource(R.string.settings_dark_mode),
                    description = stringResource(R.string.settings_dark_mode_description),
                    checked = formData.darkModeEnabled,
                    enabled = !isSaving,
                    onCheckedChange = { onFormChange(formData.copy(darkModeEnabled = it)) }
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Notifications,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = stringResource(R.string.settings_notifications),
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                SettingsSwitchRow(
                    label = stringResource(R.string.settings_notifications_enable),
                    description = stringResource(R.string.settings_notifications_description),
                    checked = formData.notificationsEnabled,
                    enabled = !isSaving,
                    onCheckedChange = { onFormChange(formData.copy(notificationsEnabled = it)) }
                )

                if (formData.notificationsEnabled) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Column(
                            modifier = Modifier.padding(start = 12.dp, top = 8.dp, end = 8.dp, bottom = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SettingsSwitchRow(
                                label = stringResource(R.string.settings_email),
                                description = stringResource(R.string.settings_email_description),
                                icon = Icons.Default.Email,
                                checked = formData.emailNotifications,
                                enabled = !isSaving && formData.notificationsEnabled,
                                onCheckedChange = { onFormChange(formData.copy(emailNotifications = it)) }
                            )
                            SettingsSwitchRow(
                                label = stringResource(R.string.settings_push),
                                description = stringResource(R.string.settings_push_description),
                                icon = Icons.Default.PhoneAndroid,
                                checked = formData.pushNotifications,
                                enabled = !isSaving && formData.notificationsEnabled,
                                onCheckedChange = { onFormChange(formData.copy(pushNotifications = it)) }
                            )
                            SettingsSwitchRow(
                                label = stringResource(R.string.settings_sms),
                                description = stringResource(R.string.settings_sms_description),
                                icon = Icons.Default.Sms,
                                checked = formData.smsNotifications,
                                enabled = !isSaving && formData.notificationsEnabled,
                                onCheckedChange = { onFormChange(formData.copy(smsNotifications = it)) }
                            )
                        }
                    }
                }
            }
        }

        Button(
            onClick = onSave,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isSaving && hasChanges,
            colors = ButtonDefaults.buttonColors(
                containerColor = SaveGreen,
                disabledContainerColor = SaveGreen.copy(alpha = 0.5f)
            )
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(stringResource(R.string.settings_saving), color = Color.White)
            } else {
                Text(stringResource(R.string.settings_save), color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsDropdown(
    label: String,
    description: String,
    icon: ImageVector,
    options: List<SelectOption>,
    selectedValue: String,
    enabled: Boolean,
    onValueChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = options.find { it.value == selectedValue }?.label ?: selectedValue

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Text(text = label, style = MaterialTheme.typography.titleSmall)
        }

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { if (enabled) expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedLabel,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                enabled = enabled
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.label) },
                        onClick = {
                            onValueChange(option.value)
                            expanded = false
                        }
                    )
                }
            }
        }

        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun SettingsSwitchRow(
    label: String,
    description: String,
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    icon: ImageVector? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (icon != null) {
                    Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
                }
                Column {
                    Text(text = label, style = MaterialTheme.typography.bodyLarge)
                    Text(
                        text = if (checked) {
                            stringResource(R.string.settings_enabled)
                        } else {
                            stringResource(R.string.settings_disabled)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                enabled = enabled
            )
        }
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
