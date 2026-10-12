package com.example.lifeflow.features.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lifeflow.core.common.Result
import com.example.lifeflow.data.repository.GoalRepositoryImpl
import com.example.lifeflow.domain.model.Goal
import com.example.lifeflow.domain.model.Milestone
import com.example.lifeflow.domain.repository.GoalRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class GoalsUiState(
    val isLoading: Boolean = true,
    val goals: List<Goal> = emptyList(),
    val selectedGoal: Goal? = null,
    val errorMessage: String? = null
)

class GoalViewModel(
    private val goalRepository: GoalRepository = GoalRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow(GoalsUiState())
    val uiState: StateFlow<GoalsUiState> = _uiState.asStateFlow()

    init {
        loadGoals()
    }

    fun loadGoals() {
        viewModelScope.launch {
            goalRepository.getGoals().collect { result ->
                when (result) {
                    is Result.Success -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            goals = result.data
                        )
                    }
                    is Result.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                    Result.Loading -> {
                        _uiState.value = _uiState.value.copy(isLoading = true)
                    }
                }
            }
        }
    }

    fun getGoalDetails(goalId: String) {
        viewModelScope.launch {
            when (val result = goalRepository.getGoalById(goalId)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(selectedGoal = result.data)
                }
                is Result.Error -> {}
                Result.Loading -> {}
            }
        }
    }

    fun createGoal(
        title: String,
        description: String,
        category: String,
        targetValue: Double,
        unit: String,
        targetDate: String,
        onSuccess: () -> Unit
    ) {
        if (title.isBlank()) return
        viewModelScope.launch {
            val goal = Goal(
                title = title,
                description = description,
                category = category,
                targetValue = targetValue,
                unit = unit,
                targetDate = targetDate
            )
            when (goalRepository.createGoal(goal)) {
                is Result.Success -> {
                    onSuccess()
                    loadGoals()
                }
                is Result.Error -> {}
                Result.Loading -> {}
            }
        }
    }

    fun addMilestone(goalId: String, title: String) {
        if (title.isBlank()) return
        viewModelScope.launch {
            val milestone = Milestone(goalId = goalId, title = title)
            when (goalRepository.addMilestone(milestone)) {
                is Result.Success -> {
                    getGoalDetails(goalId)
                    loadGoals()
                }
                is Result.Error -> {}
                Result.Loading -> {}
            }
        }
    }

    fun toggleMilestone(goalId: String, milestoneId: String, completed: Boolean) {
        viewModelScope.launch {
            when (goalRepository.toggleMilestone(milestoneId, completed)) {
                is Result.Success -> {
                    getGoalDetails(goalId)
                    loadGoals()
                }
                is Result.Error -> {}
                Result.Loading -> {}
            }
        }
    }

    fun deleteGoal(goalId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            when (goalRepository.deleteGoal(goalId)) {
                is Result.Success -> {
                    onSuccess()
                    loadGoals()
                }
                is Result.Error -> {}
                Result.Loading -> {}
            }
        }
    }
}
