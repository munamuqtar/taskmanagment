package com.example.lifeflow.data.model

import com.example.lifeflow.domain.model.Event
import com.example.lifeflow.domain.model.Goal
import com.example.lifeflow.domain.model.Milestone
import com.example.lifeflow.domain.model.Note
import com.example.lifeflow.domain.model.NotificationItem
import com.example.lifeflow.domain.model.Task
import com.example.lifeflow.domain.model.User
import com.example.lifeflow.domain.model.UserSettings
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProfileEntity(
    val id: String,
    @SerialName("full_name") val fullName: String? = null,
    val email: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
) {
    fun toDomain() = User(
        id = id,
        fullName = fullName ?: email?.substringBefore("@") ?: "User",
        email = email ?: "",
        avatarUrl = avatarUrl ?: "",
        createdAt = createdAt ?: ""
    )
}

@Serializable
data class TaskEntity(
    val id: String? = null,
    @SerialName("user_id") val userId: String? = null,
    val title: String,
    val description: String? = null,
    @SerialName("task_date") val taskDate: String? = null,
    @SerialName("start_time") val startTime: String? = null,
    @SerialName("end_time") val endTime: String? = null,
    val priority: String? = "Medium",
    val category: String? = "Personal",
    val status: String? = "Pending",
    @SerialName("reminder_minutes") val reminderMinutes: Int? = 15,
    @SerialName("repeat_type") val repeatType: String? = "None",
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("completed_at") val completedAt: String? = null
) {
    fun toDomain() = Task(
        id = id ?: "",
        userId = userId ?: "",
        title = title,
        description = description ?: "",
        taskDate = taskDate ?: "",
        startTime = startTime ?: "",
        endTime = endTime ?: "",
        priority = priority ?: "Medium",
        category = category ?: "Personal",
        status = status ?: "Pending",
        reminderMinutes = reminderMinutes ?: 15,
        repeatType = repeatType ?: "None",
        createdAt = createdAt ?: "",
        completedAt = completedAt
    )
}

@Serializable
data class GoalEntity(
    val id: String? = null,
    @SerialName("user_id") val userId: String? = null,
    val title: String,
    val description: String? = null,
    val category: String? = "Personal Growth",
    @SerialName("start_date") val startDate: String? = null,
    @SerialName("target_date") val targetDate: String? = null,
    @SerialName("target_value") val targetValue: Double? = 100.0,
    @SerialName("current_value") val currentValue: Double? = 0.0,
    val unit: String? = "Percent",
    val status: String? = "active",
    @SerialName("created_at") val createdAt: String? = null
) {
    fun toDomain(milestones: List<Milestone> = emptyList()) = Goal(
        id = id ?: "",
        userId = userId ?: "",
        title = title,
        description = description ?: "",
        category = category ?: "Personal Growth",
        startDate = startDate ?: "",
        targetDate = targetDate ?: "",
        targetValue = targetValue ?: 100.0,
        currentValue = currentValue ?: 0.0,
        unit = unit ?: "Percent",
        status = status ?: "active",
        milestones = milestones,
        createdAt = createdAt ?: ""
    )
}

@Serializable
data class MilestoneEntity(
    val id: String? = null,
    @SerialName("goal_id") val goalId: String,
    @SerialName("user_id") val userId: String? = null,
    val title: String,
    val description: String? = null,
    val completed: Boolean? = false,
    @SerialName("due_date") val dueDate: String? = null,
    @SerialName("created_at") val createdAt: String? = null
) {
    fun toDomain() = Milestone(
        id = id ?: "",
        goalId = goalId,
        userId = userId ?: "",
        title = title,
        description = description ?: "",
        completed = completed ?: false,
        dueDate = dueDate ?: "",
        createdAt = createdAt ?: ""
    )
}

@Serializable
data class EventEntity(
    val id: String? = null,
    @SerialName("user_id") val userId: String? = null,
    val title: String,
    val description: String? = null,
    @SerialName("event_date") val eventDate: String,
    @SerialName("start_time") val startTime: String? = null,
    @SerialName("end_time") val endTime: String? = null,
    val location: String? = null,
    @SerialName("meeting_url") val meetingUrl: String? = null,
    @SerialName("reminder_minutes") val reminderMinutes: Int? = 15,
    @SerialName("repeat_type") val repeatType: String? = "None",
    @SerialName("created_at") val createdAt: String? = null
) {
    fun toDomain() = Event(
        id = id ?: "",
        userId = userId ?: "",
        title = title,
        description = description ?: "",
        eventDate = eventDate,
        startTime = startTime ?: "",
        endTime = endTime ?: "",
        location = location ?: "",
        meetingUrl = meetingUrl ?: "",
        reminderMinutes = reminderMinutes ?: 15,
        repeatType = repeatType ?: "None",
        createdAt = createdAt ?: ""
    )
}

@Serializable
data class NoteEntity(
    val id: String? = null,
    @SerialName("user_id") val userId: String? = null,
    val title: String? = null,
    val content: String? = null,
    val color: String? = "#FFFFFF",
    val pinned: Boolean? = false,
    val archived: Boolean? = false,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
) {
    fun toDomain() = Note(
        id = id ?: "",
        userId = userId ?: "",
        title = title ?: "",
        content = content ?: "",
        color = color ?: "#FFFFFF",
        pinned = pinned ?: false,
        archived = archived ?: false,
        createdAt = createdAt ?: "",
        updatedAt = updatedAt ?: ""
    )
}

@Serializable
data class NotificationEntity(
    val id: String? = null,
    @SerialName("user_id") val userId: String? = null,
    val title: String,
    val message: String,
    val type: String? = "system",
    @SerialName("is_read") val isRead: Boolean? = false,
    @SerialName("created_at") val createdAt: String? = null
) {
    fun toDomain() = NotificationItem(
        id = id ?: "",
        userId = userId ?: "",
        title = title,
        message = message,
        type = type ?: "system",
        isRead = isRead ?: false,
        createdAt = createdAt ?: ""
    )
}

@Serializable
data class SettingsEntity(
    val id: String? = null,
    @SerialName("user_id") val userId: String? = null,
    val theme: String? = "system",
    @SerialName("task_reminders") val taskReminders: Boolean? = true,
    @SerialName("goal_reminders") val goalReminders: Boolean? = true,
    @SerialName("daily_summary") val dailySummary: Boolean? = true,
    @SerialName("event_reminders") val eventReminders: Boolean? = true,
    val language: String? = "en",
    @SerialName("time_format") val timeFormat: String? = "12h",
    @SerialName("week_start") val weekStart: String? = "Monday"
) {
    fun toDomain() = UserSettings(
        id = id ?: "",
        userId = userId ?: "",
        theme = theme ?: "system",
        taskReminders = taskReminders ?: true,
        goalReminders = goalReminders ?: true,
        dailySummary = dailySummary ?: true,
        eventReminders = eventReminders ?: true,
        language = language ?: "en",
        timeFormat = timeFormat ?: "12h",
        weekStart = weekStart ?: "Monday"
    )
}
