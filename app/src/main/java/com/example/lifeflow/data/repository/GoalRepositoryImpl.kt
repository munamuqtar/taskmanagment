package com.example.lifeflow.data.repository

import com.example.lifeflow.core.common.Result
import com.example.lifeflow.data.model.GoalEntity
import com.example.lifeflow.data.model.MilestoneEntity
import com.example.lifeflow.data.remote.SupabaseClient
import com.example.lifeflow.domain.model.Goal
import com.example.lifeflow.domain.model.Milestone
import com.example.lifeflow.domain.repository.GoalRepository
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class GoalRepositoryImpl : GoalRepository {

    private val postgrest = SupabaseClient.client.postgrest
    private val auth = SupabaseClient.client.auth

    private fun currentUserId(): String {
        return auth.currentUserOrNull()?.id ?: throw IllegalStateException("User not authenticated")
    }

    override fun getGoals(): Flow<Result<List<Goal>>> = flow {
        emit(Result.Loading)
        try {
            val userId = currentUserId()
            val goalEntities = postgrest.from("goals")
                .select {
                    filter { eq("user_id", userId) }
                    order("created_at", Order.DESCENDING)
                }
                .decodeList<GoalEntity>()

            val milestoneEntities = postgrest.from("goal_milestones")
                .select {
                    filter { eq("user_id", userId) }
                }
                .decodeList<MilestoneEntity>()

            val milestoneMap = milestoneEntities.map { it.toDomain() }.groupBy { it.goalId }

            val goals = goalEntities.map { goalEntity ->
                val goalId = goalEntity.id ?: ""
                val milestones = milestoneMap[goalId] ?: emptyList()
                goalEntity.toDomain(milestones)
            }

            emit(Result.Success(goals))
        } catch (e: Exception) {
            emit(Result.Error(e, e.localizedMessage ?: "Failed to fetch goals"))
        }
    }

    override suspend fun getGoalById(goalId: String): Result<Goal> {
        return try {
            val goalEntity = postgrest.from("goals")
                .select { filter { eq("id", goalId) } }
                .decodeSingle<GoalEntity>()

            val milestoneEntities = postgrest.from("goal_milestones")
                .select { filter { eq("goal_id", goalId) } }
                .decodeList<MilestoneEntity>()

            Result.Success(goalEntity.toDomain(milestoneEntities.map { it.toDomain() }))
        } catch (e: Exception) {
            Result.Error(e, e.localizedMessage ?: "Goal not found")
        }
    }

    override suspend fun createGoal(goal: Goal): Result<Goal> {
        return try {
            val userId = currentUserId()
            val entity = GoalEntity(
                userId = userId,
                title = goal.title,
                description = goal.description,
                category = goal.category,
                startDate = goal.startDate,
                targetDate = goal.targetDate,
                targetValue = goal.targetValue,
                currentValue = goal.currentValue,
                unit = goal.unit
            )

            val created = postgrest.from("goals")
                .insert(entity) { select() }
                .decodeSingle<GoalEntity>()

            Result.Success(created.toDomain())
        } catch (e: Exception) {
            Result.Error(e, e.localizedMessage ?: "Failed to create goal")
        }
    }

    override suspend fun updateGoal(goal: Goal): Result<Goal> {
        return try {
            val userId = currentUserId()
            val entity = GoalEntity(
                id = goal.id,
                userId = userId,
                title = goal.title,
                description = goal.description,
                category = goal.category,
                startDate = goal.startDate,
                targetDate = goal.targetDate,
                targetValue = goal.targetValue,
                currentValue = goal.currentValue,
                unit = goal.unit,
                status = goal.status
            )

            val updated = postgrest.from("goals")
                .update(entity) {
                    filter { eq("id", goal.id) }
                    select()
                }
                .decodeSingle<GoalEntity>()

            Result.Success(updated.toDomain(goal.milestones))
        } catch (e: Exception) {
            Result.Error(e, e.localizedMessage ?: "Failed to update goal")
        }
    }

    override suspend fun deleteGoal(goalId: String): Result<Unit> {
        return try {
            postgrest.from("goals").delete {
                filter { eq("id", goalId) }
            }
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, e.localizedMessage ?: "Failed to delete goal")
        }
    }

    override suspend fun addMilestone(milestone: Milestone): Result<Milestone> {
        return try {
            val userId = currentUserId()
            val entity = MilestoneEntity(
                goalId = milestone.goalId,
                userId = userId,
                title = milestone.title,
                description = milestone.description,
                completed = milestone.completed,
                dueDate = milestone.dueDate
            )

            val created = postgrest.from("goal_milestones")
                .insert(entity) { select() }
                .decodeSingle<MilestoneEntity>()

            Result.Success(created.toDomain())
        } catch (e: Exception) {
            Result.Error(e, e.localizedMessage ?: "Failed to add milestone")
        }
    }

    override suspend fun toggleMilestone(milestoneId: String, completed: Boolean): Result<Unit> {
        return try {
            postgrest.from("goal_milestones")
                .update({ set("completed", completed) }) {
                    filter { eq("id", milestoneId) }
                }
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, e.localizedMessage ?: "Failed to update milestone")
        }
    }

    override suspend fun deleteMilestone(milestoneId: String): Result<Unit> {
        return try {
            postgrest.from("goal_milestones").delete {
                filter { eq("id", milestoneId) }
            }
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, e.localizedMessage ?: "Failed to delete milestone")
        }
    }
}
