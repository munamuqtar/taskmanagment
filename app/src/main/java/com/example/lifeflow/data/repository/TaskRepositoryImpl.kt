package com.example.lifeflow.data.repository

import com.example.lifeflow.core.common.Result
import com.example.lifeflow.core.utils.DateTimeUtils
import com.example.lifeflow.data.model.TaskEntity
import com.example.lifeflow.data.remote.SupabaseClient
import com.example.lifeflow.domain.model.Task
import com.example.lifeflow.domain.repository.TaskRepository
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.LocalDateTime

class TaskRepositoryImpl : TaskRepository {

    private val postgrest = SupabaseClient.client.postgrest
    private val auth = SupabaseClient.client.auth

    private fun currentUserId(): String {
        return auth.currentUserOrNull()?.id ?: throw IllegalStateException("User not authenticated")
    }

    override fun getTasks(): Flow<Result<List<Task>>> = flow {
        emit(Result.Loading)
        try {
            val userId = currentUserId()
            val entities = postgrest.from("tasks")
                .select {
                    filter { eq("user_id", userId) }
                    order("created_at", Order.DESCENDING)
                }
                .decodeList<TaskEntity>()

            val tasks = entities.map { entity ->
                val task = entity.toDomain()
                if (task.status != "Completed" && DateTimeUtils.isOverdue(task.taskDate, task.endTime)) {
                    task.copy(status = "Overdue")
                } else {
                    task
                }
            }
            emit(Result.Success(tasks))
        } catch (e: Exception) {
            emit(Result.Error(e, e.localizedMessage ?: "Failed to fetch tasks"))
        }
    }

    override suspend fun getTodayTasks(): Result<List<Task>> {
        return try {
            val userId = currentUserId()
            val todayStr = DateTimeUtils.getCurrentDateString()
            val entities = postgrest.from("tasks")
                .select {
                    filter {
                        eq("user_id", userId)
                        eq("task_date", todayStr)
                    }
                    order("start_time", Order.ASCENDING)
                }
                .decodeList<TaskEntity>()

            Result.Success(entities.map { it.toDomain() })
        } catch (e: Exception) {
            Result.Error(e, e.localizedMessage ?: "Failed to fetch today's tasks")
        }
    }

    override suspend fun createTask(task: Task): Result<Task> {
        return try {
            val userId = currentUserId()
            val entity = TaskEntity(
                userId = userId,
                title = task.title,
                description = task.description,
                taskDate = task.taskDate.ifEmpty { DateTimeUtils.getCurrentDateString() },
                startTime = task.startTime,
                endTime = task.endTime,
                priority = task.priority,
                category = task.category,
                status = "Pending",
                reminderMinutes = task.reminderMinutes,
                repeatType = task.repeatType
            )
            val created = postgrest.from("tasks")
                .insert(entity) { select() }
                .decodeSingle<TaskEntity>()

            Result.Success(created.toDomain())
        } catch (e: Exception) {
            Result.Error(e, e.localizedMessage ?: "Failed to create task")
        }
    }

    override suspend fun updateTask(task: Task): Result<Task> {
        return try {
            val userId = currentUserId()
            val entity = TaskEntity(
                id = task.id,
                userId = userId,
                title = task.title,
                description = task.description,
                taskDate = task.taskDate,
                startTime = task.startTime,
                endTime = task.endTime,
                priority = task.priority,
                category = task.category,
                status = task.status,
                reminderMinutes = task.reminderMinutes,
                repeatType = task.repeatType,
                completedAt = task.completedAt
            )
            val updated = postgrest.from("tasks")
                .update(entity) {
                    filter { eq("id", task.id) }
                    select()
                }
                .decodeSingle<TaskEntity>()

            Result.Success(updated.toDomain())
        } catch (e: Exception) {
            Result.Error(e, e.localizedMessage ?: "Failed to update task")
        }
    }

    override suspend fun completeTask(taskId: String): Result<Unit> {
        return try {
            val nowStr = LocalDateTime.now().toString()
            postgrest.from("tasks")
                .update({
                    set("status", "Completed")
                    set("completed_at", nowStr)
                }) {
                    filter { eq("id", taskId) }
                }
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, e.localizedMessage ?: "Failed to complete task")
        }
    }

    override suspend fun deleteTask(taskId: String): Result<Unit> {
        return try {
            postgrest.from("tasks").delete {
                filter { eq("id", taskId) }
            }
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, e.localizedMessage ?: "Failed to delete task")
        }
    }

    override suspend fun searchTasks(query: String): Result<List<Task>> {
        return try {
            val userId = currentUserId()
            val entities = postgrest.from("tasks")
                .select {
                    filter {
                        eq("user_id", userId)
                        ilike("title", "%$query%")
                    }
                }
                .decodeList<TaskEntity>()

            Result.Success(entities.map { it.toDomain() })
        } catch (e: Exception) {
            Result.Error(e, e.localizedMessage ?: "Search failed")
        }
    }
}
