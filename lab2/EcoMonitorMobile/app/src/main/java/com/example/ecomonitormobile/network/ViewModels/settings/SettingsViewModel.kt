package com.example.ecomonitormobile.network.ViewModels.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecomonitormobile.models.Settings.UpdateSettingsDto
import com.example.ecomonitormobile.models.Settings.UserSettings
import com.example.ecomonitormobile.localization.AppLanguage
import com.example.ecomonitormobile.network.Repositories.SettingsRepository
import kotlinx.coroutines.launch

sealed class SettingsUiState {
    object Idle : SettingsUiState()
    object Loading : SettingsUiState()
    data class Loaded(val settings: UserSettings) : SettingsUiState()
    object Saving : SettingsUiState()
    data class SaveSuccess(val settings: UserSettings) : SettingsUiState()
    data class Error(val message: String) : SettingsUiState()
}

class SettingsViewModel(private val repository: SettingsRepository) : ViewModel() {
    private val _uiState = MutableLiveData<SettingsUiState>(SettingsUiState.Idle)
    val uiState: LiveData<SettingsUiState> = _uiState

    private val _formMessage = MutableLiveData<String?>(null)
    val formMessage: LiveData<String?> = _formMessage

    fun loadSettings(forceRefresh: Boolean = false) {
        if (!forceRefresh && _uiState.value is SettingsUiState.Loaded) {
            return
        }
        _uiState.value = SettingsUiState.Loading
        viewModelScope.launch {
            repository.getSettings()
                .onSuccess { settings ->
                    _uiState.value = SettingsUiState.Loaded(normalize(settings))
                }
                .onFailure { e ->
                    _uiState.value = SettingsUiState.Error(e.message ?: "Failed to load settings")
                }
        }
    }

    fun updateSettings(current: UserSettings, initial: UserSettings) {
        val dto = UpdateSettingsDto(
            language = current.language.takeIf { it != initial.language },
            measurementUnit = current.measurementUnit.takeIf { it != initial.measurementUnit },
            notificationsEnabled = current.notificationsEnabled.takeIf { it != initial.notificationsEnabled },
            darkModeEnabled = current.darkModeEnabled.takeIf { it != initial.darkModeEnabled },
            emailNotifications = current.emailNotifications.takeIf { it != initial.emailNotifications },
            pushNotifications = current.pushNotifications.takeIf { it != initial.pushNotifications },
            smsNotifications = current.smsNotifications.takeIf { it != initial.smsNotifications }
        )

        if (!dto.hasChanges()) {
            _formMessage.value = "No changes to save"
            return
        }

        _formMessage.value = null
        _uiState.value = SettingsUiState.Saving
        viewModelScope.launch {
            repository.updateSettings(dto)
                .onSuccess { settings ->
                    _uiState.value = SettingsUiState.SaveSuccess(normalize(settings))
                }
                .onFailure { e ->
                    _uiState.value = SettingsUiState.Error(e.message ?: "Failed to update settings")
                }
        }
    }

    fun resetToLoaded(settings: UserSettings) {
        _uiState.value = SettingsUiState.Loaded(settings)
    }

    fun clearFormMessage() {
        _formMessage.value = null
    }

    private fun normalize(settings: UserSettings): UserSettings = UserSettings(
        id = settings.id,
        userId = settings.userId,
        language = AppLanguage.normalize(settings.language.ifBlank { AppLanguage.DEFAULT }),
        measurementUnit = settings.measurementUnit.ifBlank { "metric" },
        notificationsEnabled = settings.notificationsEnabled,
        darkModeEnabled = settings.darkModeEnabled,
        emailNotifications = settings.emailNotifications,
        pushNotifications = settings.pushNotifications,
        smsNotifications = settings.smsNotifications,
        updatedAt = settings.updatedAt
    )
}
