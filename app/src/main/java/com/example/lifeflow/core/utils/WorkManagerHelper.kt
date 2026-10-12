package com.example.lifeflow.core.utils

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.lifeflow.workers.DailySummaryWorker
import java.util.Calendar
import java.util.concurrent.TimeUnit

object WorkManagerHelper {

    private const val DAILY_SUMMARY_WORK_TAG = "DailySummaryWorker"

    fun scheduleDailySummary(context: Context) {
        val currentDate = Calendar.getInstance()
        val dueDate = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 20) // 8:00 PM
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }

        if (dueDate.before(currentDate)) {
            dueDate.add(Calendar.HOUR_OF_DAY, 24)
        }

        val initialDelay = dueDate.timeInMillis - currentDate.timeInMillis

        val dailySummaryWorkRequest = PeriodicWorkRequestBuilder<DailySummaryWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .addTag(DAILY_SUMMARY_WORK_TAG)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            DAILY_SUMMARY_WORK_TAG,
            ExistingPeriodicWorkPolicy.KEEP,
            dailySummaryWorkRequest
        )
    }
}
