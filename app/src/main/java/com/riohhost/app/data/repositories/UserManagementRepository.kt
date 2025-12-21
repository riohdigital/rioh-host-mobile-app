package com.riohhost.app.data.repositories

import com.riohhost.app.data.api.SupabaseClient
import com.riohhost.app.data.models.UserProfile
import io.github.jan.supabase.postgrest.postgrest

class UserManagementRepository {
    private val supabase = SupabaseClient.client

    suspend fun getUsers(): List<UserProfile> {
        return try {
            // Assuming the table is named 'profiles' as is common in Supabase
            // and often exposed via a view or table that includes email
            // If email is in auth.users, we might need an RPC or a joined view.
            // Assuming the table is named 'user_profiles' to match AuthRepository
            supabase.postgrest.from("user_profiles")
                .select()
                .decodeList<UserProfile>()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
