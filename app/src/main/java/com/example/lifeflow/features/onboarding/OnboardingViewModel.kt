package com.example.lifeflow.features.onboarding

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.lifeflow.core.common.UserPreferences
import kotlinx.coroutines.launch

class OnboardingViewModel(application: Application) : AndroidViewModel(application) {

    private val userPreferences = UserPreferences(application)

    fun completeOnboarding(onCompleted: () -> Unit) {
        viewModelScope.launch {
            userPreferences.setOnboardingCompleted(true)
            onCompleted()
        }
    }
}
