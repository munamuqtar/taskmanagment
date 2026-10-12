package com.example.lifeflow

import android.app.Application
import com.example.lifeflow.core.utils.NotificationHelper
import com.example.lifeflow.core.utils.WorkManagerHelper

class LifeFlowApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannels(this)
        WorkManagerHelper.scheduleDailySummary(this)
    }
}
