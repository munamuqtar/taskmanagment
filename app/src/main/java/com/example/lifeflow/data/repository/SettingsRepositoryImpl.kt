package com.example.lifeflow.data.repository

import com.example.lifeflow.core.common.Result
import com.example.lifeflow.data.model.SettingsEntity
import com.example.lifeflow.data.remote.SupabaseClient
import com.example.lifeflow.domain.model.UserSettings
import com.example.lifeflow.domain.repository.SettingsRepository
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest

class SettingsRepositoryImpl : SettingsRepository {

    private val postgrest = SupabaseClient.client.postgrest
    private val auth = SupabaseClient.client.auth

    private fun currentUserId(): String {
        return auth.currentUserOrNull()?.id ?: throw IllegalStateException("User not authenticated")
    }

    override suspend fun getSettings(): Result<UserSettings> {
        return try {
            val userId = currentUserId()
            val entity = postgrest.from("user_settings")
                .select { filter { eq("user_id", userId) } }
                .decodeSingleOrNull<SettingsEntity>()

            if (entity != null) {
                Result.Success(entity.toDomain())
            } else {
                val newSettings = SettingsEntity(userId = userId)
                postgrest.from("user_settings").insert(newSettings)
                Result.Success(newSettings.toDomain())
            }
        } catch (e: Exception) {
            Result.Error(e, e.localizedMessage ?: "Failed to load user settings")
        }
    }

    override suspend fun updateSettings(settings: UserSettings): Result<UserSettings> {
        return try {
            val userId = currentUserId()
            val entity = SettingsEntity(
                id = settings.id.ifEmpty { null },
                userId = userId,
                theme = settings.theme,
                taskReminders = settings.taskReminders,
                goalReminders = settings.goalReminders,
                dailySummary = settings.dailySummary,
                eventReminders = settings.eventReminders,
                language = settings.language,
                timeFormat = settings.timeFormat,
                weekStart = settings.weekStart
            )

            postgrest.from("user_settings")
                .upsert(entity)

            Result.Success(settings)
        } catch (e: Exception) {
            Result.Error(e, e.localizedMessage ?: "Failed to save settings")
        }
    }
}
