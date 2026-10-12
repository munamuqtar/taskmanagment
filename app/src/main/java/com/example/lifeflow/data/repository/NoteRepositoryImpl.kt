package com.example.lifeflow.data.repository

import com.example.lifeflow.core.common.Result
import com.example.lifeflow.data.model.NoteEntity
import com.example.lifeflow.data.remote.SupabaseClient
import com.example.lifeflow.domain.model.Note
import com.example.lifeflow.domain.repository.NoteRepository
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class NoteRepositoryImpl : NoteRepository {

    private val postgrest = SupabaseClient.client.postgrest
    private val auth = SupabaseClient.client.auth

    private fun currentUserId(): String {
        return auth.currentUserOrNull()?.id ?: throw IllegalStateException("User not authenticated")
    }

    override fun getNotes(): Flow<Result<List<Note>>> = flow {
        emit(Result.Loading)
        try {
            val userId = currentUserId()
            val entities = postgrest.from("notes")
                .select {
                    filter { eq("user_id", userId) }
                    order("created_at", Order.DESCENDING)
                }
                .decodeList<NoteEntity>()

            emit(Result.Success(entities.map { it.toDomain() }))
        } catch (e: Exception) {
            emit(Result.Error(e, e.localizedMessage ?: "Failed to fetch notes"))
        }
    }

    override suspend fun createNote(note: Note): Result<Note> {
        return try {
            val userId = currentUserId()
            val entity = NoteEntity(
                userId = userId,
                title = note.title,
                content = note.content,
                color = note.color,
                pinned = note.pinned,
                archived = note.archived
            )

            val created = postgrest.from("notes")
                .insert(entity) { select() }
                .decodeSingle<NoteEntity>()

            Result.Success(created.toDomain())
        } catch (e: Exception) {
            Result.Error(e, e.localizedMessage ?: "Failed to create note")
        }
    }

    override suspend fun updateNote(note: Note): Result<Note> {
        return try {
            val userId = currentUserId()
            val entity = NoteEntity(
                id = note.id,
                userId = userId,
                title = note.title,
                content = note.content,
                color = note.color,
                pinned = note.pinned,
                archived = note.archived
            )

            val updated = postgrest.from("notes")
                .update(entity) {
                    filter { eq("id", note.id) }
                    select()
                }
                .decodeSingle<NoteEntity>()

            Result.Success(updated.toDomain())
        } catch (e: Exception) {
            Result.Error(e, e.localizedMessage ?: "Failed to update note")
        }
    }

    override suspend fun deleteNote(noteId: String): Result<Unit> {
        return try {
            postgrest.from("notes").delete {
                filter { eq("id", noteId) }
            }
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, e.localizedMessage ?: "Failed to delete note")
        }
    }

    override suspend fun togglePin(noteId: String, pinned: Boolean): Result<Unit> {
        return try {
            postgrest.from("notes")
                .update({ set("pinned", pinned) }) {
                    filter { eq("id", noteId) }
                }
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, e.localizedMessage ?: "Failed to update pin status")
        }
    }

    override suspend fun toggleArchive(noteId: String, archived: Boolean): Result<Unit> {
        return try {
            postgrest.from("notes")
                .update({ set("archived", archived) }) {
                    filter { eq("id", noteId) }
                }
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, e.localizedMessage ?: "Failed to update archive status")
        }
    }
}
