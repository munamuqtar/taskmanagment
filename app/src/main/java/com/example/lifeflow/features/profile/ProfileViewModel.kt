package com.example.lifeflow.features.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lifeflow.core.common.Result
import com.example.lifeflow.data.repository.AuthRepositoryImpl
import com.example.lifeflow.data.repository.GoalRepositoryImpl
import com.example.lifeflow.data.repository.ProfileRepositoryImpl
import com.example.lifeflow.data.repository.TaskRepositoryImpl
import com.example.lifeflow.domain.model.User
import com.example.lifeflow.domain.repository.AuthRepository
import com.example.lifeflow.domain.repository.GoalRepository
import com.example.lifeflow.domain.repository.ProfileRepository
import com.example.lifeflow.domain.repository.TaskRepository
import com.example.lifeflow.domain.usecase.CalculateProductivityScoreUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProfileUiState(
    val isLoading: Boolean = true,
    val user: User? = null,
    val completedTasksCount: Int = 0,
    val goalsCount: Int = 0,
    val streakDays: Int = 0,
    val errorMessage: String? = null
)

class ProfileViewModel(
    private val profileRepository: ProfileRepository = ProfileRepositoryImpl(),
    private val authRepository: AuthRepository = AuthRepositoryImpl(),
    private val taskRepository: TaskRepository = TaskRepositoryImpl(),
    private val goalRepository: GoalRepository = GoalRepositoryImpl(),
    private val calculateProductivityScoreUseCase: CalculateProductivityScoreUseCase = CalculateProductivityScoreUseCase()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfileData()
    }

    fun loadProfileData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            when (val userResult = profileRepository.getProfile()) {
                is Result.Success -> {
                    val user = userResult.data
                    taskRepository.getTasks().collect { tasksResult ->
                        if (tasksResult is Result.Success) {
                            goalRepository.getGoals().collect { goalsResult ->
                                if (goalsResult is Result.Success) {
                                    val analytics = calculateProductivityScoreUseCase(
                                        tasks = tasksResult.data,
                                        goals = goalsResult.data
                                    )
                                    _uiState.value = _uiState.value.copy(
                                        isLoading = false,
                                        user = user,
                                        completedTasksCount = analytics.completedTasks,
                                        goalsCount = goalsResult.data.size,
                                        streakDays = analytics.streakDays
                                    )
                                }
                            }
                        }
                    }
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = userResult.message
                    )
                }
                Result.Loading -> {}
            }
        }
    }

    fun updateProfile(fullName: String, avatarUrl: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            when (profileRepository.updateProfile(fullName, avatarUrl)) {
                is Result.Success -> {
                    onSuccess()
                    loadProfileData()
                }
                is Result.Error -> {}
                Result.Loading -> {}
            }
        }
    }

    fun logout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            onSuccess()
        }
    }
}
