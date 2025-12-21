package com.riohhost.app.data.repositories

import com.riohhost.app.data.api.SupabaseClient
import com.riohhost.app.data.models.UserProfile
import io.github.jan.supabase.gotrue.gotrue
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest

class AuthRepository {
    private val supabase = SupabaseClient.client

    suspend fun signIn(email: String, password: String) {
        supabase.gotrue.loginWith(Email) {
            this.email = email
            this.password = password
        }
    }

    suspend fun signOut() {
        supabase.gotrue.logout()
    }

    suspend fun getCurrentUser(): UserProfile? {
        val userId = supabase.gotrue.currentSessionOrNull()?.user?.id ?: return null
        return try {
            supabase.postgrest.from("user_profiles")
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeSingle<UserProfile>()
        } catch (e: Exception) {
            null
        }
    }

    fun isUserLoggedIn(): Boolean {
        return supabase.gotrue.currentSessionOrNull() != null
    }
}
