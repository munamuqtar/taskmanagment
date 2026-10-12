package com.example.lifeflow.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lifeflow.core.common.Result
import com.example.lifeflow.core.utils.DateTimeUtils
import com.example.lifeflow.data.repository.AuthRepositoryImpl
import com.example.lifeflow.data.repository.NotificationRepositoryImpl
import com.example.lifeflow.data.repository.ProfileRepositoryImpl
import com.example.lifeflow.data.repository.TaskRepositoryImpl
import com.example.lifeflow.domain.model.Task
import com.example.lifeflow.domain.model.User
import com.example.lifeflow.domain.repository.AuthRepository
import com.example.lifeflow.domain.repository.NotificationRepository
import com.example.lifeflow.domain.repository.ProfileRepository
import com.example.lifeflow.domain.repository.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = true,
    val user: User? = null,
    val greeting: String = "Good Morning",
    val todayTasks: List<Task> = emptyList(),
    val completedCount: Int = 0,
    val totalTodayCount: Int = 0,
    val dailyProgressPercent: Int = 0,
    val totalTasks: Int = 0,
    val inProgressTasks: Int = 0,
    val completedTasks: Int = 0,
    val overdueTasks: Int = 0,
    val unreadNotificationsCount: Int = 0,
    val errorMessage: String? = null
)

class HomeViewModel(
    private val taskRepository: TaskRepository = TaskRepositoryImpl(),
    private val profileRepository: ProfileRepository = ProfileRepositoryImpl(),
    private val authRepository: AuthRepository = AuthRepositoryImpl(),
    private val notificationRepository: NotificationRepository = NotificationRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val currentUser = authRepository.getCurrentUser()
            val greetingMsg = DateTimeUtils.getGreetingMessage()

            // Fetch Today's tasks
            when (val tasksResult = taskRepository.getTodayTasks()) {
                is Result.Success -> {
                    val todayTasks = tasksResult.data
                    val completed = todayTasks.count { it.status == "Completed" }
                    val totalToday = todayTasks.size
                    val percent = if (totalToday > 0) ((completed.toDouble() / totalToday) * 100).toInt() else 0

                    // Fetch all tasks for stats breakdown
                    taskRepository.getTasks().collect { allResult ->
                        if (allResult is Result.Success) {
                            val allTasks = allResult.data
                            val total = allTasks.size
                            val inProg = allTasks.count { it.status == "In Progress" || it.status == "Pending" }
                            val comp = allTasks.count { it.status == "Completed" }
                            val overdue = allTasks.count {
                                it.status == "Overdue" || (it.status != "Completed" && DateTimeUtils.isOverdue(it.taskDate, it.endTime))
                            }

                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                user = currentUser,
                                greeting = greetingMsg,
                                todayTasks = todayTasks,
                                completedCount = completed,
                                totalTodayCount = totalToday,
                                dailyProgressPercent = percent,
                                totalTasks = total,
                                inProgressTasks = inProg,
                                completedTasks = comp,
                                overdueTasks = overdue
                            )
                        }
                    }
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        user = currentUser,
                        greeting = greetingMsg,
                        errorMessage = tasksResult.message
                    )
                }
                Result.Loading -> {}
            }
        }
    }

    fun toggleTaskComplete(task: Task) {
        viewModelScope.launch {
            if (task.status == "Completed") return@launch
            when (taskRepository.completeTask(task.id)) {
                is Result.Success -> loadDashboardData()
                is Result.Error -> {}
                Result.Loading -> {}
            }
        }
    }
}
