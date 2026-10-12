package com.example.lifeflow.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.lifeflow.core.utils.NotificationHelper

class TaskReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val taskId = inputData.getString("task_id") ?: return Result.failure()
        val title = inputData.getString("task_title") ?: "Task Reminder"

        NotificationHelper.showNotification(
            applicationContext,
            NotificationHelper.CHANNEL_TASKS,
            taskId.hashCode(),
            "Reminder: $title",
            "Your task deadline is coming up!"
        )

        return Result.success()
    }
}