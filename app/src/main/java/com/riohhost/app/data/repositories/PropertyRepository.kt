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
            null
        }
    }
    suspend fun createProperty(data: com.riohhost.app.data.models.PropertyFormData, userId: String): Result<Property> {
        val submissionData = mapOf(
            "name" to data.name,
            "nickname" to data.nickname,
            "address" to data.address,
            "property_type" to data.property_type,
            "status" to data.status,
            "airbnb_link" to data.airbnb_link,
            "booking_link" to data.booking_link,
            "commission_rate" to (data.commission_rate / 100.0),  // Converter!
            "cleaning_fee" to data.cleaning_fee.toBigDecimalOrNull(),
            "base_nightly_price" to data.base_nightly_price?.toBigDecimalOrNull(),
            "max_guests" to data.max_guests,
            "notes" to data.notes,
            "default_checkin_time" to data.default_checkin_time,
            "default_checkout_time" to data.default_checkout_time,
            "created_by" to userId
        )
        
        return try {
            val result = supabase.postgrest.from("properties")
                .insert(submissionData)
                .decodeSingle<Property>()
            Result.success(result)
        } catch (e: Exception) {
            android.util.Log.e("PropertyRepo", "Erro ao criar propriedade: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun updateProperty(id: String, data: com.riohhost.app.data.models.PropertyFormData): Result<Property> {
        val submissionData = mapOf(
            "name" to data.name,
            "nickname" to data.nickname,
            "address" to data.address,
            "property_type" to data.property_type,
            "status" to data.status,
            "airbnb_link" to data.airbnb_link,
            "booking_link" to data.booking_link,
            "commission_rate" to (data.commission_rate / 100.0),  // Converter!
            "cleaning_fee" to data.cleaning_fee.toBigDecimalOrNull(),
            "base_nightly_price" to data.base_nightly_price?.toBigDecimalOrNull(),
            "max_guests" to data.max_guests,
            "notes" to data.notes,
            "default_checkin_time" to data.default_checkin_time,
            "default_checkout_time" to data.default_checkout_time
        )
        
        return try {
            val result = supabase.postgrest.from("properties")
                .update(submissionData) {
                    eq("id", id)
                }
                .decodeSingle<Property>()
            Result.success(result)
        } catch (e: Exception) {
            android.util.Log.e("PropertyRepo", "Erro ao atualizar propriedade: ${e.message}")
            Result.failure(e)
        }
    }
}
