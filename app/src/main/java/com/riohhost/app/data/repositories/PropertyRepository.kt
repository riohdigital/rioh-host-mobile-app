package com.riohhost.app.data.repositories

import com.riohhost.app.data.api.SupabaseClient
import com.riohhost.app.data.models.Property
import io.github.jan.supabase.postgrest.postgrest

class PropertyRepository {
    private val supabase = SupabaseClient.client

    suspend fun getProperties(): List<Property> {
        return try {
            supabase.postgrest.from("properties")
                .select()
                .decodeList<Property>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getPropertyById(id: String): Property? {
        return try {
            supabase.postgrest.from("properties")
                .select {
                    filter { eq("id", id) }
                }
                .decodeSingle<Property>()
        } catch (e: Exception) {
            null
        }
    }
}
