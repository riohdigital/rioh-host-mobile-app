package com.riohhost.app.data.repositories

import com.riohhost.app.data.api.SupabaseClient
import com.riohhost.app.data.models.Property
import com.riohhost.app.data.models.PropertyCreate
import com.riohhost.app.data.models.PropertyUpdate
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
            result
        } catch (e: Exception) {
            android.util.Log.e("PropertyRepo", "ERRO ao buscar propriedades: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun getPropertyById(id: String): Property? {
        return try {
            supabase.postgrest.from("properties")
                .select { filter { eq("id", id) } }
                .decodeSingle<Property>()
        } catch (e: Exception) {
            android.util.Log.e("PropertyRepo", "ERRO ao buscar propriedade: ${e.message}", e)
            null
        }
    }

    suspend fun createProperty(property: PropertyCreate): Result<Property> {
        return try {
            android.util.Log.d("PropertyRepo", "Criando propriedade: ${property.name}")
            val result = supabase.postgrest.from("properties")
                .insert(property)
                .decodeSingle<Property>()
            android.util.Log.d("PropertyRepo", "Propriedade criada: ${result.id}")
            Result.success(result)
        } catch (e: Exception) {
            android.util.Log.e("PropertyRepo", "ERRO ao criar propriedade: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun updateProperty(id: String, updates: PropertyUpdate): Result<Property> {
        return try {
            android.util.Log.d("PropertyRepo", "Atualizando propriedade: $id")
            val result = supabase.postgrest.from("properties")
                .update(updates) { filter { eq("id", id) } }
                .decodeSingle<Property>()
            android.util.Log.d("PropertyRepo", "Propriedade atualizada: ${result.id}")
            Result.success(result)
        } catch (e: Exception) {
            android.util.Log.e("PropertyRepo", "ERRO ao atualizar propriedade: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun deleteProperty(id: String): Result<Unit> {
        return try {
            android.util.Log.d("PropertyRepo", "Deletando propriedade: $id")
            supabase.postgrest.from("properties")
                .delete { filter { eq("id", id) } }
            android.util.Log.d("PropertyRepo", "Propriedade deletada: $id")
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("PropertyRepo", "ERRO ao deletar propriedade: ${e.message}", e)
            Result.failure(e)
        }
    }
}
