package com.example.lifeflow.data.repository

import com.example.lifeflow.core.common.Result
import com.example.lifeflow.data.model.ProfileEntity
import com.example.lifeflow.data.remote.SupabaseClient
import com.example.lifeflow.domain.model.User
import com.example.lifeflow.domain.repository.AuthRepository
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest

class AuthRepositoryImpl : AuthRepository {

    private val auth = SupabaseClient.client.auth
    private val postgrest = SupabaseClient.client.postgrest

    override suspend fun register(fullName: String, email: String, password: String): Result<User> {
        return try {
            auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }

            val currentSession = auth.currentSessionOrNull()
            val userId = currentSession?.user?.id ?: auth.currentUserOrNull()?.id
            if (userId == null) {
                return Result.Error(Exception("Registration successful. Please check your email for verification."))
            }

            // Create Profile in Supabase
            val profile = ProfileEntity(
                id = userId,
                fullName = fullName,
                email = email,
                avatarUrl = ""
            )
            postgrest.from("profiles").insert(profile)

            Result.Success(User(id = userId, fullName = fullName, email = email))
        } catch (e: Exception) {
            Result.Error(e, e.localizedMessage ?: "Failed to register")
        }
    }

    override suspend fun login(email: String, password: String): Result<User> {
        return try {
            auth.signInWith(Email) {
                this.email = email
                this.password = password
            }

            val userId = auth.currentUserOrNull()?.id
                ?: return Result.Error(Exception("Failed to retrieve user session."))

            val profile = postgrest.from("profiles")
                .select { filter { eq("id", userId) } }
                .decodeSingleOrNull<ProfileEntity>()

            val user = profile?.toDomain() ?: User(id = userId, fullName = email.substringBefore("@"), email = email)
            Result.Success(user)
        } catch (e: Exception) {
            Result.Error(e, "Invalid email or password")
        }
    }

    override suspend fun logout(): Result<Unit> {
        return try {
            auth.signOut()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, e.localizedMessage ?: "Failed to sign out")
        }
    }

    override suspend fun forgotPassword(email: String): Result<Unit> {
        return try {
            auth.resetPasswordForEmail(email)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, e.localizedMessage ?: "Failed to send password reset email")
        }
    }

    override suspend fun getCurrentUser(): User? {
        val currentUser = auth.currentUserOrNull() ?: return null
        return try {
            val profile = postgrest.from("profiles")
                .select { filter { eq("id", currentUser.id) } }
                .decodeSingleOrNull<ProfileEntity>()

            profile?.toDomain() ?: User(
                id = currentUser.id,
                fullName = currentUser.email?.substringBefore("@") ?: "User",
                email = currentUser.email ?: ""
            )
        } catch (e: Exception) {
            User(id = currentUser.id, fullName = currentUser.email?.substringBefore("@") ?: "User", email = currentUser.email ?: "")
        }
    }

    override fun isUserLoggedIn(): Boolean {
        return auth.currentSessionOrNull() != null || auth.currentUserOrNull() != null
    }
}
