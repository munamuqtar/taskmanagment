package com.example.lifeflow.features.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lifeflow.core.common.Result
import com.example.lifeflow.data.repository.AuthRepositoryImpl
import com.example.lifeflow.domain.model.User
import com.example.lifeflow.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AuthUiState {
    object Idle : AuthUiState
    object Loading : AuthUiState
    data class Success(val user: User) : AuthUiState
    data class Error(val message: String) : AuthUiState
    data class PasswordResetSent(val email: String) : AuthUiState
}

class AuthViewModel(
    private val authRepository: AuthRepository = AuthRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = AuthUiState.Error("Please fill in all required fields")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            when (val result = authRepository.login(email.trim(), password.trim())) {
                is Result.Success -> _uiState.value = AuthUiState.Success(result.data)
                is Result.Error -> _uiState.value = AuthUiState.Error(result.message)
                Result.Loading -> _uiState.value = AuthUiState.Loading
            }
        }
    }

    fun register(fullName: String, email: String, password: String, confirmPass: String) {
        if (fullName.isBlank() || email.isBlank() || password.isBlank()) {
            _uiState.value = AuthUiState.Error("Please fill in all fields")
            return
        }
        if (password.length < 6) {
            _uiState.value = AuthUiState.Error("Password must contain at least 6 characters")
            return
        }
        if (password != confirmPass) {
            _uiState.value = AuthUiState.Error("Passwords do not match")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            when (val result = authRepository.register(fullName.trim(), email.trim(), password.trim())) {
                is Result.Success -> _uiState.value = AuthUiState.Success(result.data)
                is Result.Error -> _uiState.value = AuthUiState.Error(result.message)
                Result.Loading -> _uiState.value = AuthUiState.Loading
            }
        }
    }

    fun forgotPassword(email: String) {
        if (email.isBlank()) {
            _uiState.value = AuthUiState.Error("Please enter your registered email address")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            when (val result = authRepository.forgotPassword(email.trim())) {
                is Result.Success -> _uiState.value = AuthUiState.PasswordResetSent(email)
                is Result.Error -> _uiState.value = AuthUiState.Error(result.message)
                Result.Loading -> _uiState.value = AuthUiState.Loading
            }
        }
    }

    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }
}
