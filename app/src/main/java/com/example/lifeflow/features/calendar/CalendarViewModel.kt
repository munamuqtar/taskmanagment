package com.example.lifeflow.features.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lifeflow.core.common.Result
import com.example.lifeflow.core.utils.DateTimeUtils
import com.example.lifeflow.data.repository.EventRepositoryImpl
import com.example.lifeflow.data.repository.TaskRepositoryImpl
import com.example.lifeflow.domain.model.Event
import com.example.lifeflow.domain.model.Task
import com.example.lifeflow.domain.repository.EventRepository
import com.example.lifeflow.domain.repository.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

data class CalendarUiState(
    val isLoading: Boolean = true,
    val selectedDate: String = DateTimeUtils.getCurrentDateString(),
    val events: List<Event> = emptyList(),
    val tasksForDate: List<Task> = emptyList(),
    val errorMessage: String? = null
)

class CalendarViewModel(
    private val eventRepository: EventRepository = EventRepositoryImpl(),
    private val taskRepository: TaskRepository = TaskRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    init {
        loadDataForDate(_uiState.value.selectedDate)
    }

    fun selectDate(dateString: String) {
        _uiState.value = _uiState.value.copy(selectedDate = dateString)
        loadDataForDate(dateString)
    }

    private fun loadDataForDate(dateString: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            when (val eventsResult = eventRepository.getEventsForDate(dateString)) {
                is Result.Success -> {
                    taskRepository.getTasks().collect { tasksResult ->
                        if (tasksResult is Result.Success) {
                            val dayTasks = tasksResult.data.filter { it.taskDate == dateString }
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                events = eventsResult.data,
                                tasksForDate = dayTasks
                            )
                        }
                    }
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = eventsResult.message
                    )
                }
                Result.Loading -> {}
            }
        }
    }

    fun createEvent(
        title: String,
        description: String,
        eventDate: String,
        startTime: String,
        endTime: String,
        location: String,
        meetingUrl: String,
        onSuccess: () -> Unit
    ) {
        if (title.isBlank() || eventDate.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Title and date are required")
            return
        }

        viewModelScope.launch {
            val event = Event(
                title = title,
                description = description,
                eventDate = eventDate,
                startTime = startTime,
                endTime = endTime,
                location = location,
                meetingUrl = meetingUrl
            )

            when (val result = eventRepository.createEvent(event)) {
                is Result.Success -> {
                    onSuccess()
                    loadDataForDate(eventDate)
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(errorMessage = result.message)
                }
                Result.Loading -> {}
            }
        }
    }

    fun deleteEvent(eventId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            when (eventRepository.deleteEvent(eventId)) {
                is Result.Success -> {
                    onSuccess()
                    loadDataForDate(_uiState.value.selectedDate)
                }
                is Result.Error -> {}
                Result.Loading -> {}
            }
        }
    }
}
