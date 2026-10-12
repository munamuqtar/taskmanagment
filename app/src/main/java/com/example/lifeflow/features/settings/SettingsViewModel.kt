package com.example.lifeflow.features.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.lifeflow.core.common.Result
import com.example.lifeflow.core.common.UserPreferences
import com.example.lifeflow.data.repository.AuthRepositoryImpl
import com.example.lifeflow.data.repository.ProfileRepositoryImpl
import com.example.lifeflow.data.repository.SettingsRepositoryImpl
import com.example.lifeflow.domain.model.UserSettings
import com.example.lifeflow.domain.repository.AuthRepository
import com.example.lifeflow.domain.repository.ProfileRepository
import com.example.lifeflow.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SettingsUiState(
    val isLoading: Boolean = true,
    val settings: UserSettings = UserSettings(),
    val errorMessage: String? = null
)

class SettingsViewModel(
    application: Application,
    private val settingsRepository: SettingsRepository = SettingsRepositoryImpl(),
    private val profileRepository: ProfileRepository = ProfileRepositoryImpl(),
    private val authRepository: AuthRepository = AuthRepositoryImpl()
) : AndroidViewModel(application) {

    private val userPreferences = UserPreferences(application)
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    fun loadSettings() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            when (val result = settingsRepository.getSettings()) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        settings = result.data
                    )
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
                Result.Loading -> {}
            }
        }
    }

    fun setTheme(themeMode: String) {
        val updated = _uiState.value.settings.copy(theme = themeMode)
        _uiState.value = _uiState.value.copy(settings = updated)
        viewModelScope.launch {
            userPreferences.setThemePreference(themeMode)
            settingsRepository.updateSettings(updated)
        }
    }

    fun toggleTaskReminders(enabled: Boolean) {
        val updated = _uiState.value.settings.copy(taskReminders = enabled)
        _uiState.value = _uiState.value.copy(settings = updated)
        viewModelScope.launch { settingsRepository.updateSettings(updated) }
    }

    fun toggleGoalReminders(enabled: Boolean) {
        val updated = _uiState.value.settings.copy(goalReminders = enabled)
        _uiState.value = _uiState.value.copy(settings = updated)
        viewModelScope.launch { settingsRepository.updateSettings(updated) }
    }

    fun toggleDailySummary(enabled: Boolean) {
        val updated = _uiState.value.settings.copy(dailySummary = enabled)
        _uiState.value = _uiState.value.copy(settings = updated)
        viewModelScope.launch { settingsRepository.updateSettings(updated) }
    }

    fun deleteAccount(onSuccess: () -> Unit) {
        viewModelScope.launch {
            when (profileRepository.deleteAccount()) {
                is Result.Success -> onSuccess()
                is Result.Error -> {}
                Result.Loading -> {}
            }
        }
    }
}
