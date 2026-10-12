package com.example.lifeflow.data.repository

import com.example.lifeflow.core.common.Result
import com.example.lifeflow.data.model.ProfileEntity
import com.example.lifeflow.data.remote.SupabaseClient
import com.example.lifeflow.domain.model.User
import com.example.lifeflow.domain.repository.ProfileRepository
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage

class ProfileRepositoryImpl : ProfileRepository {

    private val postgrest = SupabaseClient.client.postgrest
    private val auth = SupabaseClient.client.auth
    private val storage = SupabaseClient.client.storage

    private fun currentUserId(): String {
        return auth.currentUserOrNull()?.id ?: throw IllegalStateException("User not authenticated")
    }

    override suspend fun getProfile(): Result<User> {
        return try {
            val userId = currentUserId()
            val profile = postgrest.from("profiles")
                .select { filter { eq("id", userId) } }
                .decodeSingleOrNull<ProfileEntity>()

            if (profile != null) {
                Result.Success(profile.toDomain())
            } else {
                val email = auth.currentUserOrNull()?.email ?: ""
                val defaultUser = User(id = userId, fullName = email.substringBefore("@"), email = email)
                Result.Success(defaultUser)
            }
        } catch (e: Exception) {
            Result.Error(e, e.localizedMessage ?: "Failed to load profile")
        }
    }

    override suspend fun updateProfile(fullName: String, avatarUrl: String): Result<User> {
        return try {
            val userId = currentUserId()
            postgrest.from("profiles")
                .update({
                    set("full_name", fullName)
                    set("avatar_url", avatarUrl)
                }) {
                    filter { eq("id", userId) }
                }

            val updatedProfile = postgrest.from("profiles")
                .select { filter { eq("id", userId) } }
                .decodeSingle<ProfileEntity>()

            Result.Success(updatedProfile.toDomain())
        } catch (e: Exception) {
            Result.Error(e, e.localizedMessage ?: "Failed to update profile")
        }
    }

    override suspend fun deleteAccount(): Result<Unit> {
        return try {
            val userId = currentUserId()
            // Delete user profile data in Supabase (triggers cascading deletes where configured)
            postgrest.from("profiles").delete {
                filter { eq("id", userId) }
            }
            auth.signOut()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, e.localizedMessage ?: "Failed to delete account")
        }
    }
}
