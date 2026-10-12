package com.example.lifeflow.features.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lifeflow.core.common.Result
import com.example.lifeflow.data.repository.NotificationRepositoryImpl
import com.example.lifeflow.domain.model.NotificationItem
import com.example.lifeflow.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class NotificationsUiState(
    val isLoading: Boolean = true,
    val notifications: List<NotificationItem> = emptyList(),
    val errorMessage: String? = null
)

class NotificationViewModel(
    private val notificationRepository: NotificationRepository = NotificationRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    init {
        loadNotifications()
    }

    fun loadNotifications() {
        viewModelScope.launch {
            notificationRepository.getNotifications().collect { result ->
                when (result) {
                    is Result.Success -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            notifications = result.data
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

    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            when (notificationRepository.markAsRead(notificationId)) {
                is Result.Success -> loadNotifications()
                is Result.Error -> {}
                Result.Loading -> {}
            }
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            when (notificationRepository.markAllAsRead()) {
                is Result.Success -> loadNotifications()
                is Result.Error -> {}
                Result.Loading -> {}
            }
        }
    }

    fun deleteNotification(notificationId: String) {
        viewModelScope.launch {
            when (notificationRepository.deleteNotification(notificationId)) {
                is Result.Success -> loadNotifications()
                is Result.Error -> {}
                Result.Loading -> {}
            }
        }
    }
}
