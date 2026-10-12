package com.example.lifeflow.features.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lifeflow.core.common.Result
import com.example.lifeflow.data.repository.GoalRepositoryImpl
import com.example.lifeflow.data.repository.TaskRepositoryImpl
import com.example.lifeflow.domain.model.AnalyticsData
import com.example.lifeflow.domain.repository.GoalRepository
import com.example.lifeflow.domain.repository.TaskRepository
import com.example.lifeflow.domain.usecase.CalculateProductivityScoreUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class AnalyticsTimeframe { WEEK, MONTH, YEAR }

data class AnalyticsUiState(
    val isLoading: Boolean = true,
    val selectedTimeframe: AnalyticsTimeframe = AnalyticsTimeframe.WEEK,
    val analyticsData: AnalyticsData = AnalyticsData(),
    val errorMessage: String? = null
)

class AnalyticsViewModel(
    private val taskRepository: TaskRepository = TaskRepositoryImpl(),
    private val goalRepository: GoalRepository = GoalRepositoryImpl(),
    private val calculateProductivityScoreUseCase: CalculateProductivityScoreUseCase = CalculateProductivityScoreUseCase()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    init {
        loadAnalyticsData()
    }

    fun selectTimeframe(timeframe: AnalyticsTimeframe) {
        _uiState.value = _uiState.value.copy(selectedTimeframe = timeframe)
        loadAnalyticsData()
    }

    fun loadAnalyticsData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            taskRepository.getTasks().collect { tasksResult ->
                if (tasksResult is Result.Success) {
                    goalRepository.getGoals().collect { goalsResult ->
                        if (goalsResult is Result.Success) {
                            val computedData = calculateProductivityScoreUseCase(
                                tasks = tasksResult.data,
                                goals = goalsResult.data
                            )
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                analyticsData = computedData
                            )
                        }
                    }
                }
            }
        }
    }
}
