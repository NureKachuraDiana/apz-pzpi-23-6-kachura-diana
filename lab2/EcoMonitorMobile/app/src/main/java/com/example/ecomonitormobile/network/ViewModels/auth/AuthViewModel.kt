package com.example.ecomonitormobile.network.ViewModels.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.ecomonitormobile.models.Login.LoginRequest
import com.example.ecomonitormobile.models.Login.LoginResponse
import com.example.ecomonitormobile.network.Repositories.AuthRepository
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

sealed class AuthUiState {
    object Loading : AuthUiState()
    data class Success(val user: LoginResponse) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
    object Unauthorized : AuthUiState()
}

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {
    private val _uiState = MutableLiveData<AuthUiState>()
    val uiState: LiveData<AuthUiState> = _uiState

    private val _currentUser = MutableLiveData<LoginResponse?>()
    val currentUser: LiveData<LoginResponse?> = _currentUser

    fun login(email: String, password: String) {
        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            try {
                val loginResult = repository.login(LoginRequest(email, password))
                loginResult.onSuccess { _: LoginResponse ->
                    val userResult = repository.getCurrentUser()
                    userResult.onSuccess { fullUser ->
                        _currentUser.value = fullUser
                        _uiState.value = AuthUiState.Success(fullUser)
                    }.onFailure { e ->
                        _uiState.value = AuthUiState.Error("Failed to fetch user: ${e.message}")
                    }
                }.onFailure { exception ->
                    _uiState.value = AuthUiState.Error(exception.message ?: "Unknown error")
                }
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message ?: "Unexpected error")
            }
        }
    }

    fun checkSession() {
        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            val result = repository.getCurrentUser()
            result.onSuccess { user ->
                _currentUser.value = user
                _uiState.value = AuthUiState.Success(user)
            }.onFailure {
                _uiState.value = AuthUiState.Unauthorized
            }
        }
    }

    fun logout() {
        // Временно: сбрасываем состояние
        _currentUser.value = null
        _uiState.value = AuthUiState.Unauthorized
    }
}