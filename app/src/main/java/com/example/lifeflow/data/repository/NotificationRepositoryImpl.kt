package com.example.lifeflow.data.repository

import com.example.lifeflow.core.common.Result
import com.example.lifeflow.data.model.NotificationEntity
import com.example.lifeflow.data.remote.SupabaseClient
import com.example.lifeflow.domain.model.NotificationItem
import com.example.lifeflow.domain.repository.NotificationRepository
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class NotificationRepositoryImpl : NotificationRepository {

    private val postgrest = SupabaseClient.client.postgrest
    private val auth = SupabaseClient.client.auth

    private fun currentUserId(): String {
        return auth.currentUserOrNull()?.id ?: throw IllegalStateException("User not authenticated")
    }

    override fun getNotifications(): Flow<Result<List<NotificationItem>>> = flow {
        emit(Result.Loading)
        try {
            val userId = currentUserId()
            val entities = postgrest.from("notifications")
                .select {
                    filter { eq("user_id", userId) }
                    order("created_at", Order.DESCENDING)
                }
                .decodeList<NotificationEntity>()

            emit(Result.Success(entities.map { it.toDomain() }))
        } catch (e: Exception) {
            emit(Result.Error(e, e.localizedMessage ?: "Failed to fetch notifications"))
        }
    }

    override suspend fun markAsRead(notificationId: String): Result<Unit> {
        return try {
            postgrest.from("notifications")
                .update({ set("is_read", true) }) {
                    filter { eq("id", notificationId) }
                }
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, e.localizedMessage ?: "Failed to mark notification as read")
        }
    }

    override suspend fun markAllAsRead(): Result<Unit> {
        return try {
            val userId = currentUserId()
            postgrest.from("notifications")
                .update({ set("is_read", true) }) {
                    filter { eq("user_id", userId) }
                }
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, e.localizedMessage ?: "Failed to mark all as read")
        }
    }

    override suspend fun deleteNotification(notificationId: String): Result<Unit> {
        return try {
            postgrest.from("notifications").delete {
                filter { eq("id", notificationId) }
            }
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, e.localizedMessage ?: "Failed to delete notification")
        }
    }

    override suspend fun createNotification(title: String, message: String, type: String): Result<Unit> {
        return try {
            val userId = currentUserId()
            val entity = NotificationEntity(
                userId = userId,
                title = title,
                message = message,
                type = type,
                isRead = false
            )
            postgrest.from("notifications").insert(entity)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, e.localizedMessage ?: "Failed to create notification record")
        }
    }
}
