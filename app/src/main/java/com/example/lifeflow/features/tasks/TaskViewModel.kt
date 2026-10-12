package com.example.lifeflow.features.tasks

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.lifeflow.core.common.Result
import com.example.lifeflow.core.utils.DateTimeUtils
import com.example.lifeflow.core.utils.NotificationHelper
import com.example.lifeflow.data.repository.TaskRepositoryImpl
import com.example.lifeflow.domain.model.Task
import com.example.lifeflow.domain.repository.TaskRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

enum class TaskFilterTab { ALL, TODAY, UPCOMING, COMPLETED }

data class TasksUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val tasks: List<Task> = emptyList(),
    val filteredTasks: List<Task> = emptyList(),
    val selectedTab: TaskFilterTab = TaskFilterTab.ALL,
    val searchQuery: String = "",
    val selectedCategory: String? = null,
    val selectedPriority: String? = null,
    val errorMessage: String? = null,
    val actionSuccessMessage: String? = null
)

@OptIn(FlowPreview::class)
class TaskViewModel @JvmOverloads constructor(
    application: Application,
    private val taskRepository: TaskRepository = TaskRepositoryImpl()
) : AndroidViewModel(application) {

    private companion object {
        const val TAG = "TaskViewModel"
    }

    private val _uiState = MutableStateFlow(TasksUiState())
    val uiState: StateFlow<TasksUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    private var loadJob: Job? = null

    init {
        loadTasks()

        viewModelScope.launch {
            _searchQuery
                .debounce(300)
                .collect { query ->
                    _uiState.update { state ->
                        val updated = state.copy(searchQuery = query)
                        updated.copy(filteredTasks = computeFiltered(updated))
                    }
                }
        }
    }

    fun loadTasks() {
        // Cancel any previous collector so they don't pile up
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            taskRepository.getTasks().collect { result ->
                when (result) {
                    is Result.Success -> {
                        _uiState.update { state ->
                            val updated = state.copy(isLoading = false, tasks = result.data)
                            updated.copy(filteredTasks = computeFiltered(updated))
                        }
                    }
                    is Result.Error -> {
                        _uiState.update {
                            it.copy(isLoading = false, errorMessage = result.message)
                        }
                    }
                    Result.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun selectTab(tab: TaskFilterTab) = updateFilters { it.copy(selectedTab = tab) }

    fun setCategoryFilter(category: String?) = updateFilters { it.copy(selectedCategory = category) }

    fun setPriorityFilter(priority: String?) = updateFilters { it.copy(selectedPriority = priority) }

    private fun updateFilters(transform: (TasksUiState) -> TasksUiState) {
        _uiState.update { state ->
            val updated = transform(state)
            updated.copy(filteredTasks = computeFiltered(updated))
        }
    }

    private fun computeFiltered(state: TasksUiState): List<Task> {
        val todayStr = DateTimeUtils.getCurrentDateString()

        return state.tasks.filter { task ->
            val matchesTab = when (state.selectedTab) {
                TaskFilterTab.ALL -> true
                TaskFilterTab.TODAY -> task.taskDate == todayStr
                // yyyy-MM-dd strings sort chronologically
                TaskFilterTab.UPCOMING -> task.taskDate > todayStr
                TaskFilterTab.COMPLETED -> task.status == "Completed"
            }

            val matchesQuery = state.searchQuery.isBlank() ||
                    task.title.contains(state.searchQuery, ignoreCase = true) ||
                    task.description.contains(state.searchQuery, ignoreCase = true)

            val matchesCategory = state.selectedCategory?.let { task.category == it } ?: true
            val matchesPriority = state.selectedPriority?.let { task.priority == it } ?: true

            matchesTab && matchesQuery && matchesCategory && matchesPriority
        }
    }

    fun createTask(
        title: String,
        description: String,
        taskDate: String,
        startTime: String,
        endTime: String,
        priority: String,
        category: String,
        reminderMinutes: Int,
        repeatType: String,
        onSuccess: () -> Unit
    ) {
        if (title.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Task title is required") }
            return
        }

        // Prevent double submit
        if (_uiState.value.isSaving) return
        _uiState.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            val task = Task(
                title = title.trim(),
                description = description.trim(),
                taskDate = taskDate.ifBlank { DateTimeUtils.getCurrentDateString() },
                startTime = startTime,
                endTime = endTime,
                priority = priority,
                category = category,
                reminderMinutes = reminderMinutes,
                repeatType = repeatType
            )

            when (val result = taskRepository.createTask(task)) {
                is Result.Success -> {
                    scheduleReminder(
                        taskId = result.data.id,
                        taskTitle = result.data.title,
                        taskDate = task.taskDate,
                        startTime = startTime,
                        reminderMinutes = reminderMinutes
                    )
                    _uiState.update {
                        it.copy(isSaving = false, actionSuccessMessage = "Task created successfully")
                    }
                    onSuccess()
                    loadTasks()
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isSaving = false, errorMessage = result.message) }
                }
                Result.Loading -> {
                    _uiState.update { it.copy(isSaving = false) }
                }
            }
        }
    }

    private fun scheduleReminder(
        taskId: String,
        taskTitle: String,
        taskDate: String,
        startTime: String,
        reminderMinutes: Int
    ) {
        if (startTime.isBlank()) return

        try {
            val date = LocalDate.parse(taskDate)
            val time = LocalTime.parse(startTime)
            val triggerDateTime = LocalDateTime.of(date, time)
                .minusMinutes(reminderMinutes.coerceAtLeast(0).toLong())

            // Don't schedule alarms in the past
            if (triggerDateTime.isAfter(LocalDateTime.now())) {
                NotificationHelper.scheduleTaskAlarm(
                    getApplication(),
                    taskId,
                    taskTitle,
                    triggerDateTime
                )
            } else {
                Log.w(TAG, "Reminder time is in the past, alarm not scheduled")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule reminder", e)
        }
    }

    fun updateTask(task: Task, onSuccess: () -> Unit) {
        viewModelScope.launch {
            when (val result = taskRepository.updateTask(task)) {
                is Result.Success -> {
                    _uiState.update { it.copy(actionSuccessMessage = "Task updated successfully") }
                    onSuccess()
                    loadTasks()
                }
                is Result.Error -> {
                    _uiState.update { it.copy(errorMessage = result.message) }
                }
                Result.Loading -> {}
            }
        }
    }

    fun completeTask(task: Task) {
        viewModelScope.launch {
            when (val result = taskRepository.completeTask(task.id)) {
                is Result.Success -> {
                    NotificationHelper.cancelTaskAlarm(getApplication(), task.id)
                    loadTasks()
                }
                is Result.Error -> {
                    _uiState.update { it.copy(errorMessage = result.message) }
                }
                Result.Loading -> {}
            }
        }
    }

    fun deleteTask(taskId: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            when (val result = taskRepository.deleteTask(taskId)) {
                is Result.Success -> {
                    NotificationHelper.cancelTaskAlarm(getApplication(), taskId)
                    _uiState.update { it.copy(actionSuccessMessage = "Task deleted") }
                    onSuccess()
                    loadTasks()
                }
                is Result.Error -> {
                    _uiState.update { it.copy(errorMessage = result.message) }
                }
                Result.Loading -> {}
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, actionSuccessMessage = null) }
    }
}