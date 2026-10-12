package com.example.lifeflow.data.repository

import com.example.lifeflow.core.common.Result
import com.example.lifeflow.data.model.EventEntity
import com.example.lifeflow.data.remote.SupabaseClient
import com.example.lifeflow.domain.model.Event
import com.example.lifeflow.domain.repository.EventRepository
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class EventRepositoryImpl : EventRepository {

    private val postgrest = SupabaseClient.client.postgrest
    private val auth = SupabaseClient.client.auth

    private fun currentUserId(): String {
        return auth.currentUserOrNull()?.id ?: throw IllegalStateException("User not authenticated")
    }

    override fun getEvents(): Flow<Result<List<Event>>> = flow {
        emit(Result.Loading)
        try {
            val userId = currentUserId()
            val entities = postgrest.from("events")
                .select {
                    filter { eq("user_id", userId) }
                    order("event_date", Order.ASCENDING)
                }
                .decodeList<EventEntity>()

            emit(Result.Success(entities.map { it.toDomain() }))
        } catch (e: Exception) {
            emit(Result.Error(e, e.localizedMessage ?: "Failed to fetch events"))
        }
    }

    override suspend fun getEventsForDate(date: String): Result<List<Event>> {
        return try {
            val userId = currentUserId()
            val entities = postgrest.from("events")
                .select {
                    filter {
                        eq("user_id", userId)
                        eq("event_date", date)
                    }
                    order("start_time", Order.ASCENDING)
                }
                .decodeList<EventEntity>()

            Result.Success(entities.map { it.toDomain() })
        } catch (e: Exception) {
            Result.Error(e, e.localizedMessage ?: "Failed to fetch events for date")
        }
    }

    override suspend fun createEvent(event: Event): Result<Event> {
        return try {
            val userId = currentUserId()
            val entity = EventEntity(
                userId = userId,
                title = event.title,
                description = event.description,
                eventDate = event.eventDate,
                startTime = event.startTime,
                endTime = event.endTime,
                location = event.location,
                meetingUrl = event.meetingUrl,
                reminderMinutes = event.reminderMinutes,
                repeatType = event.repeatType
            )

            val created = postgrest.from("events")
                .insert(entity) { select() }
                .decodeSingle<EventEntity>()

            Result.Success(created.toDomain())
        } catch (e: Exception) {
            Result.Error(e, e.localizedMessage ?: "Failed to create event")
        }
    }

    override suspend fun updateEvent(event: Event): Result<Event> {
        return try {
            val userId = currentUserId()
            val entity = EventEntity(
                id = event.id,
                userId = userId,
                title = event.title,
                description = event.description,
                eventDate = event.eventDate,
                startTime = event.startTime,
                endTime = event.endTime,
                location = event.location,
                meetingUrl = event.meetingUrl,
                reminderMinutes = event.reminderMinutes,
                repeatType = event.repeatType
            )

            val updated = postgrest.from("events")
                .update(entity) {
                    filter { eq("id", event.id) }
                    select()
                }
                .decodeSingle<EventEntity>()

            Result.Success(updated.toDomain())
        } catch (e: Exception) {
            Result.Error(e, e.localizedMessage ?: "Failed to update event")
        }
    }

    override suspend fun deleteEvent(eventId: String): Result<Unit> {
        return try {
            postgrest.from("events").delete {
                filter { eq("id", eventId) }
            }
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, e.localizedMessage ?: "Failed to delete event")
        }
    }
}
