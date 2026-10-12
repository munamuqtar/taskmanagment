package com.example.lifeflow.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.lifeflow.core.common.Result as AppResult
import com.example.lifeflow.core.utils.NotificationHelper
import com.example.lifeflow.data.repository.TaskRepositoryImpl

class DailySummaryWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val taskRepository = TaskRepositoryImpl()

        return try {
            when (val result = taskRepository.getTodayTasks()) {
                is AppResult.Success -> {
                    val tasks = result.data
                    val total = tasks.size
                    val completed = tasks.count { it.status == "Completed" }
                    val remaining = total - completed
                    val percent = if (total > 0) ((completed.toDouble() / total) * 100).toInt() else 100

                    val title = "Daily Summary Digest 🌙"
                    val message = if (total == 0) {
                        "No tasks scheduled today. Ready for tomorrow?"
                    } else {
                        "You completed $completed of $total tasks today ($percent%). $remaining remaining."
                    }

                    NotificationHelper.showNotification(
                        applicationContext,
                        NotificationHelper.CHANNEL_SUMMARY,
                        1001,
                        title,
                        message
                    )

                    Result.success()
                }
                else -> Result.failure()
            }
        } catch (e: Exception) {
            Result.failure()
        }
    }
}