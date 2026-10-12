package com.example.lifeflow.domain.usecase

import com.example.lifeflow.core.utils.DateTimeUtils
import com.example.lifeflow.domain.model.AnalyticsData
import com.example.lifeflow.domain.model.Goal
import com.example.lifeflow.domain.model.Task
import java.time.LocalDate

class CalculateProductivityScoreUseCase {

    operator fun invoke(tasks: List<Task>, goals: List<Goal>): AnalyticsData {
        if (tasks.isEmpty() && goals.isEmpty()) {
            return AnalyticsData(
                totalTasks = 0,
                completedTasks = 0,
                pendingTasks = 0,
                overdueTasks = 0,
                completionRate = 0f,
                productivityScore = 0,
                scoreRating = "Needs Improvement",
                streakDays = 0,
                categoryDistribution = emptyMap(),
                weeklyCompletion = emptyList()
            )
        }

        val totalTasks = tasks.size
        val completedTasks = tasks.count { it.status == "Completed" }
        val overdueTasks = tasks.count {
            it.status == "Overdue" || (it.status != "Completed" && DateTimeUtils.isOverdue(it.taskDate, it.endTime))
        }
        val pendingTasks = totalTasks - completedTasks - overdueTasks

        val taskCompletionRate = if (totalTasks > 0) completedTasks.toFloat() / totalTasks else 0f

        // Average Goal Progress
        val avgGoalProgress = if (goals.isNotEmpty()) {
            goals.map { it.progressPercentage }.average().toFloat() / 100f
        } else {
            0f
        }

        // On-time completion
        val onTimeTasks = tasks.count { it.status == "Completed" && !DateTimeUtils.isOverdue(it.taskDate, it.endTime) }
        val onTimeRate = if (completedTasks > 0) onTimeTasks.toFloat() / completedTasks else 1.0f

        // Calculate Streak
        val streakDays = calculateStreak(tasks)
        val consistencyScore = (streakDays.toFloat() / 7f).coerceAtMost(1.0f)

        // Formula: 40% Task + 25% Goal + 20% On-time + 15% Consistency
        val totalScore = (
            (taskCompletionRate * 40) +
            (avgGoalProgress * 25) +
            (onTimeRate * 20) +
            (consistencyScore * 15)
        ).toInt().coerceIn(0, 100)

        val scoreRating = when (totalScore) {
            in 0..39 -> "Needs Improvement"
            in 40..59 -> "Getting Started"
            in 60..79 -> "Good"
            in 80..89 -> "Great"
            else -> "Excellent"
        }

        // Category Distribution
        val categories = tasks.groupBy { it.category }
        val categoryDistribution = categories.mapValues { (_, list) ->
            if (totalTasks > 0) (list.size.toFloat() / totalTasks) * 100f else 0f
        }

        // Weekly completion trends
        val today = LocalDate.now()
        val daysOfWeek = (6 downTo 0).map { today.minusDays(it.toLong()) }
        val weeklyCompletion = daysOfWeek.map { date ->
            val dateStr = date.toString()
            val dayName = date.dayOfWeek.name.take(3)
            val count = tasks.count { it.taskDate == dateStr && it.status == "Completed" }
            Pair(dayName, count)
        }

        return AnalyticsData(
            totalTasks = totalTasks,
            completedTasks = completedTasks,
            pendingTasks = pendingTasks,
            overdueTasks = overdueTasks,
            completionRate = taskCompletionRate * 100f,
            productivityScore = totalScore,
            scoreRating = scoreRating,
            streakDays = streakDays,
            categoryDistribution = categoryDistribution,
            weeklyCompletion = weeklyCompletion
        )
    }

    private fun calculateStreak(tasks: List<Task>): Int {
        val completedDates = tasks
            .filter { it.status == "Completed" && it.completedAt != null }
            .mapNotNull {
                try {
                    it.completedAt?.take(10)
                } catch (e: Exception) {
                    null
                }
            }
            .toSet()

        var streak = 0
        var checkDate = LocalDate.now()

        while (completedDates.contains(checkDate.toString())) {
            streak++
            checkDate = checkDate.minusDays(1)
        }

        return streak
    }
}
