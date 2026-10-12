package com.example.lifeflow.core.utils

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.lifeflow.MainActivity
import com.example.lifeflow.R
import java.time.LocalDateTime
import java.time.ZoneId

object NotificationHelper {

    const val CHANNEL_TASKS = "channel_tasks"
    const val CHANNEL_GOALS = "channel_goals"
    const val CHANNEL_EVENTS = "channel_events"
    const val CHANNEL_SUMMARY = "channel_summary"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val tasksChannel = NotificationChannel(
                CHANNEL_TASKS,
                "Task Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply { description = "Notifications for upcoming task deadlines" }

            val goalsChannel = NotificationChannel(
                CHANNEL_GOALS,
                "Goal Milestones",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "Notifications for goal achievements" }

            val eventsChannel = NotificationChannel(
                CHANNEL_EVENTS,
                "Calendar Events",
                NotificationManager.IMPORTANCE_HIGH
            ).apply { description = "Notifications for calendar events" }

            val summaryChannel = NotificationChannel(
                CHANNEL_SUMMARY,
                "Daily Summary",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "Daily productivity summary digest" }

            notificationManager.createNotificationChannels(
                listOf(tasksChannel, goalsChannel, eventsChannel, summaryChannel)
            )
        }
    }

    fun showNotification(
        context: Context,
        channelId: String,
        notificationId: Int,
        title: String,
        message: String
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, builder.build())
    }

    fun scheduleTaskAlarm(
        context: Context,
        taskId: String,
        title: String,
        triggerDateTime: LocalDateTime
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, TaskAlarmReceiver::class.java).apply {
            putExtra("EXTRA_TASK_ID", taskId)
            putExtra("EXTRA_TITLE", title)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerMillis = triggerDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        if (triggerMillis > System.currentTimeMillis()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
            }
        }
    }

    fun cancelTaskAlarm(context: Context, taskId: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, TaskAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId.hashCode(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
        }
    }

    class TaskAlarmReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val title = intent.getStringExtra("EXTRA_TITLE") ?: "Task Reminder"
            val taskId = intent.getStringExtra("EXTRA_TASK_ID") ?: ""
            showNotification(
                context,
                CHANNEL_TASKS,
                taskId.hashCode(),
                "Reminder: $title",
                "Your scheduled task is coming up soon!"
            )
        }
    }
}
