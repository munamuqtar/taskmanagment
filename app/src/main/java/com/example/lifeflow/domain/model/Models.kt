package com.example.lifeflow.domain.model

data class User(
    val id: String,
    val fullName: String,
    val email: String,
    val avatarUrl: String = "",
    val createdAt: String = ""
)

data class Task(
    val id: String = "",
    val userId: String = "",
    val title: String,
    val description: String = "",
    val taskDate: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val priority: String = "Medium", // Low, Medium, High
    val category: String = "Personal",
    val status: String = "Pending", // Pending, In Progress, Completed, Overdue, Cancelled
    val reminderMinutes: Int = 15,
    val repeatType: String = "None",
    val createdAt: String = "",
    val completedAt: String? = null
)

data class Goal(
    val id: String = "",
    val userId: String = "",
    val title: String,
    val description: String = "",
    val category: String = "Personal Growth",
    val startDate: String = "",
    val targetDate: String = "",
    val targetValue: Double = 100.0,
    val currentValue: Double = 0.0,
    val unit: String = "Percent",
    val status: String = "active",
    val milestones: List<Milestone> = emptyList(),
    val createdAt: String = ""
) {
    val progressPercentage: Int
        get() {
            return if (milestones.isNotEmpty()) {
                val completedCount = milestones.count { it.completed }
                ((completedCount.toDouble() / milestones.size) * 100).toInt().coerceIn(0, 100)
            } else if (targetValue > 0) {
                ((currentValue / targetValue) * 100).toInt().coerceIn(0, 100)
            } else {
                0
            }
        }
}

data class Milestone(
    val id: String = "",
    val goalId: String = "",
    val userId: String = "",
    val title: String,
    val description: String = "",
    val completed: Boolean = false,
    val dueDate: String = "",
    val createdAt: String = ""
)

data class Event(
    val id: String = "",
    val userId: String = "",
    val title: String,
    val description: String = "",
    val eventDate: String,
    val startTime: String = "",
    val endTime: String = "",
    val location: String = "",
    val meetingUrl: String = "",
    val reminderMinutes: Int = 15,
    val repeatType: String = "None",
    val createdAt: String = ""
)

data class Note(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val content: String = "",
    val color: String = "#FFFFFF",
    val pinned: Boolean = false,
    val archived: Boolean = false,
    val createdAt: String = "",
    val updatedAt: String = ""
)

data class NotificationItem(
    val id: String = "",
    val userId: String = "",
    val title: String,
    val message: String,
    val type: String = "system",
    val isRead: Boolean = false,
    val createdAt: String = ""
)

data class UserSettings(
    val id: String = "",
    val userId: String = "",
    val theme: String = "system", // light, dark, system
    val taskReminders: Boolean = true,
    val goalReminders: Boolean = true,
    val dailySummary: Boolean = true,
    val eventReminders: Boolean = true,
    val language: String = "en",
    val timeFormat: String = "12h",
    val weekStart: String = "Monday"
)

data class AnalyticsData(
    val totalTasks: Int = 0,
    val completedTasks: Int = 0,
    val pendingTasks: Int = 0,
    val overdueTasks: Int = 0,
    val completionRate: Float = 0f,
    val productivityScore: Int = 0,
    val scoreRating: String = "Getting Started",
    val streakDays: Int = 0,
    val categoryDistribution: Map<String, Float> = emptyMap(),
    val weeklyCompletion: List<Pair<String, Int>> = emptyList()
)
