package com.riohhost.app.data.repositories

import com.riohhost.app.data.api.SupabaseClient
import com.riohhost.app.data.models.Property
import io.github.jan.supabase.postgrest.postgrest

class PropertyRepository {
    private val supabase = SupabaseClient.client

    suspend fun getProperties(): List<Property> {
        return try {
            android.util.Log.d("PropertyRepo", "Iniciando busca de propriedades...")
            val result = supabase.postgrest.from("properties")
                .select()
                .decodeList<Property>()
            android.util.Log.d("PropertyRepo", "Encontradas ${result.size} propriedades")
            if (result.isNotEmpty()) {
                android.util.Log.d("PropertyRepo", "Primeira propriedade: ${result.first()}")
            }
            result
        } catch (e: Exception) {
            android.util.Log.e("PropertyRepo", "ERRO ao buscar propriedades: ${e.message}", e)
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
            android.util.Log.e("PropertyRepo", "ERRO ao buscar propriedade por ID: ${e.message}", e)
            null
        }
    }
}
