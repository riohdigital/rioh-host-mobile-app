package com.riohhost.app.data.repositories

import com.riohhost.app.data.api.SupabaseClient
import com.riohhost.app.data.models.Property
import io.github.jan.supabase.postgrest.postgrest

class PropertyRepository {
    private val supabase = SupabaseClient.client

    suspend fun getProperties(): List<Property> {
        return try {
            println("PropertyRepo: Iniciando busca de propriedades...")
            val result = supabase.postgrest.from("properties")
                .select()
                .decodeList<Property>()
            println("PropertyRepo: Encontradas ${result.size} propriedades")
            if (result.isNotEmpty()) {
                println("PropertyRepo: Primeira propriedade: ${result.first()}")
            }
            result
        } catch (e: Exception) {
            println("PropertyRepo: ERRO ao buscar propriedades: ${e.message}")
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
            println("PropertyRepo: Erro ao criar propriedade: ${e.message}")
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
                    filter {
                        eq("id", id)
                    }
                }
                .decodeSingle<Property>()
            Result.success(result)
        } catch (e: Exception) {
            println("PropertyRepo: Erro ao atualizar propriedade: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun getPropertyCleaners(propertyId: String): List<com.riohhost.app.data.models.PropertyCleaner> {
        return try {
             // 1. Get cleaner IDs for property
             val cleanersResult = supabase.postgrest.from("cleaner_properties")
                 .select(columns = io.github.jan.supabase.postgrest.query.Columns.list("user_id")) {
                     filter { eq("property_id", propertyId) }
                 }
                 .decodeList<Map<String, String>>()
             
             val userIds = cleanersResult.mapNotNull { it["user_id"] }
             
             if (userIds.isEmpty()) return emptyList()
             
             // 2. Fetch profiles
             val profiles = supabase.postgrest.from("user_profiles")
                 .select {
                     filter { isIn("user_id", userIds) }
                 }
                 .decodeList<com.riohhost.app.data.models.UserProfile>()
             
             // 3. Fetch cleaner specific details (phone)
             val cleanerDetails = try {
                 supabase.postgrest.from("cleaner_profiles")
                     .select {
                         filter { isIn("user_id", userIds) }
                     }
                     .decodeList<com.riohhost.app.data.models.CleanerProfile>()
             } catch (e: Exception) {
                 emptyList()
             }
             
             // 4. Merge
             return profiles.map { profile ->
                 val details = cleanerDetails.find { it.userId == profile.userId }
                 com.riohhost.app.data.models.PropertyCleaner(
                     userId = profile.userId,
                     fullName = profile.fullName,
                     email = profile.email,
                     phone = details?.phone,
                     pix = null
                 )
             }
        } catch (e: Exception) {
            println("PropertyRepo: Erro ao buscar faxineiras vinculadas: ${e.message}")
            emptyList()
        }
    }
}
