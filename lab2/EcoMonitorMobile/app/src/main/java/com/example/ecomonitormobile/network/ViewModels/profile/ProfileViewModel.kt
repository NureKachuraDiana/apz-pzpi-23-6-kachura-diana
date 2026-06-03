// network/ViewModels/ProfileViewModel.kt
package com.example.ecomonitormobile.network.ViewModels.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecomonitormobile.models.Login.LoginResponse
import com.example.ecomonitormobile.models.Profile.UpdateProfile
import com.example.ecomonitormobile.network.Repositories.ProfileRepository
import kotlinx.coroutines.launch

sealed class ProfileUiState {
    object Idle : ProfileUiState()
    object Loading : ProfileUiState()
    data class Success(val user: LoginResponse) : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
}

class ProfileViewModel(private val repository: ProfileRepository) : ViewModel() {
    private val _uiState = MutableLiveData<ProfileUiState>()
    val uiState: LiveData<ProfileUiState> = _uiState

    fun updateProfile(currentUser: LoginResponse, firstName: String?, lastName: String?) {
        _uiState.value = ProfileUiState.Loading
        viewModelScope.launch {
            val dto = UpdateProfile(
                firstName = if (firstName.isNullOrBlank()) null else firstName,
                lastName = if (lastName.isNullOrBlank()) null else lastName
            )
            val result = repository.updateProfile(dto)
            result.onSuccess { updatedUser ->
                _uiState.value = ProfileUiState.Success(updatedUser)
            }.onFailure { e ->
                _uiState.value = ProfileUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun resetState() {
        _uiState.value = ProfileUiState.Idle
    }
}