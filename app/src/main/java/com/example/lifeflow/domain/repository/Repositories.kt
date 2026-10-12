package com.example.lifeflow.domain.repository

import com.example.lifeflow.core.common.Result
import com.example.lifeflow.domain.model.AnalyticsData
import com.example.lifeflow.domain.model.Event
import com.example.lifeflow.domain.model.Goal
import com.example.lifeflow.domain.model.Milestone
import com.example.lifeflow.domain.model.Note
import com.example.lifeflow.domain.model.NotificationItem
import com.example.lifeflow.domain.model.Task
import com.example.lifeflow.domain.model.User
import com.example.lifeflow.domain.model.UserSettings
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun register(fullName: String, email: String, password: String): Result<User>
    suspend fun login(email: String, password: String): Result<User>
    suspend fun logout(): Result<Unit>
    suspend fun forgotPassword(email: String): Result<Unit>
    suspend fun getCurrentUser(): User?
    fun isUserLoggedIn(): Boolean
}

interface TaskRepository {
    fun getTasks(): Flow<Result<List<Task>>>
    suspend fun getTodayTasks(): Result<List<Task>>
    suspend fun createTask(task: Task): Result<Task>
    suspend fun updateTask(task: Task): Result<Task>
    suspend fun completeTask(taskId: String): Result<Unit>
    suspend fun deleteTask(taskId: String): Result<Unit>
    suspend fun searchTasks(query: String): Result<List<Task>>
}

interface GoalRepository {
    fun getGoals(): Flow<Result<List<Goal>>>
    suspend fun getGoalById(goalId: String): Result<Goal>
    suspend fun createGoal(goal: Goal): Result<Goal>
    suspend fun updateGoal(goal: Goal): Result<Goal>
    suspend fun deleteGoal(goalId: String): Result<Unit>
    suspend fun addMilestone(milestone: Milestone): Result<Milestone>
    suspend fun toggleMilestone(milestoneId: String, completed: Boolean): Result<Unit>
    suspend fun deleteMilestone(milestoneId: String): Result<Unit>
}

interface EventRepository {
    fun getEvents(): Flow<Result<List<Event>>>
    suspend fun getEventsForDate(date: String): Result<List<Event>>
    suspend fun createEvent(event: Event): Result<Event>
    suspend fun updateEvent(event: Event): Result<Event>
    suspend fun deleteEvent(eventId: String): Result<Unit>
}

interface NoteRepository {
    fun getNotes(): Flow<Result<List<Note>>>
    suspend fun createNote(note: Note): Result<Note>
    suspend fun updateNote(note: Note): Result<Note>
    suspend fun deleteNote(noteId: String): Result<Unit>
    suspend fun togglePin(noteId: String, pinned: Boolean): Result<Unit>
    suspend fun toggleArchive(noteId: String, archived: Boolean): Result<Unit>
}

interface NotificationRepository {
    fun getNotifications(): Flow<Result<List<NotificationItem>>>
    suspend fun markAsRead(notificationId: String): Result<Unit>
    suspend fun markAllAsRead(): Result<Unit>
    suspend fun deleteNotification(notificationId: String): Result<Unit>
    suspend fun createNotification(title: String, message: String, type: String): Result<Unit>
}

interface ProfileRepository {
    suspend fun getProfile(): Result<User>
    suspend fun updateProfile(fullName: String, avatarUrl: String): Result<User>
    suspend fun deleteAccount(): Result<Unit>
}

interface SettingsRepository {
    suspend fun getSettings(): Result<UserSettings>
    suspend fun updateSettings(settings: UserSettings): Result<UserSettings>
}
