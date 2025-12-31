package com.riohhost.app.data.repositories

import com.riohhost.app.data.api.SupabaseClient
import com.riohhost.app.data.models.Reservation
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc

class ReservationRepository {
    private val supabase = SupabaseClient.client

    suspend fun getReservations(propertyId: String? = null): List<Reservation> {
        return try {
            android.util.Log.d("ReservationRepo", "Iniciando busca de reservas...")
            val result = supabase.postgrest.from("reservations")
                .select {
                    if (propertyId != null) {
                        filter { eq("property_id", propertyId) }
                    }
                }
                .decodeList<Reservation>()
            android.util.Log.d("ReservationRepo", "Encontradas ${result.size} reservas")
            if (result.isNotEmpty()) {
                android.util.Log.d("ReservationRepo", "Primeira reserva: ${result.first()}")
            }
            result
        } catch (e: Exception) {
            android.util.Log.e("ReservationRepo", "ERRO ao buscar reservas: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Get reservations filtered by date range, properties, and platform.
     * Uses overlap logic: check_out >= startDate AND check_in <= endDate
     * This captures all reservations that overlap with the period.
     */
    suspend fun getReservationsFiltered(
        startDate: String,
        endDate: String,
        propertyIds: List<String>? = null,
        platform: String? = null
    ): List<Reservation> {
        return try {
            android.util.Log.d("ReservationRepo", "Buscando reservas: $startDate a $endDate, props: $propertyIds, platform: $platform")
            
            var query = supabase.postgrest.from("reservations").select()
            
            // CRITICAL: Overlap logic - captures reservations that overlap with period
            // check_out >= startDate (reservation ends after period starts)
            // check_in <= endDate (reservation starts before period ends)
            query = query.gte("check_out_date", startDate)
            query = query.lte("check_in_date", endDate)
            
            // Platform filter
            if (!platform.isNullOrEmpty() && platform != "all") {
                query = query.eq("platform", platform)
            }
            
            val result = query.decodeList<Reservation>()
            
            // Property filter (applied client-side due to API limitations)
            val filteredResult = if (!propertyIds.isNullOrEmpty() && !propertyIds.contains("todas")) {
                result.filter { reservation -> 
                    reservation.propertyId?.let { propertyIds.contains(it) } ?: false
                }
            } else {
                result
            }
            
            android.util.Log.d("ReservationRepo", "Encontradas ${filteredResult.size} reservas filtradas")
            filteredResult
        } catch (e: Exception) {
            android.util.Log.e("ReservationRepo", "ERRO ao buscar reservas filtradas: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun getReservationById(id: String): Reservation? {
        return try {
            supabase.postgrest.from("reservations")
                .select {
                    filter { eq("id", id) }
                }
                .decodeSingle<Reservation>()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getCleanerReservations(cleanerId: String): List<Reservation> {
        return try {
            supabase.postgrest.rpc("fn_get_cleaner_reservations", mapOf("cleaner_id" to cleanerId))
                .decodeList<Reservation>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getAvailableReservations(cleanerId: String): List<Reservation> {
        return try {
            supabase.postgrest.rpc("fn_get_available_reservations", mapOf("cleaner_id" to cleanerId))
                .decodeList<Reservation>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun assignCleaning(reservationId: String, cleanerId: String) {
        try {
            supabase.postgrest.rpc("assign_cleaning_with_permissions", 
                mapOf("reservation_id" to reservationId, "cleaner_id" to cleanerId))
        } catch (e: Exception) {
            // Handle error or rethrow
        }
    }

    suspend fun toggleCleaningStatus(reservationId: String) {
        try {
            supabase.postgrest.rpc("fn_toggle_cleaning_status", mapOf("reservation_id" to reservationId))
        } catch (e: Exception) {
            // Handle error or rethrow
        }
    }

    suspend fun getCleanersForProperty(propertyId: String): Result<List<com.riohhost.app.data.models.CleanerInfo>> {
        return try {
            val response = supabase.postgrest
                .rpc("fn_get_property_cleaners_for_user", mapOf("p_property_id" to propertyId))
                .decodeList<com.riohhost.app.data.models.CleanerInfo>()
            Result.success(response)
        } catch (e: Exception) {
            android.util.Log.e("ReservationRepo", "Erro ao buscar faxineiras: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun createReservation(data: com.riohhost.app.data.models.ReservationFormData): Result<Reservation> {
        val (cleanerId, cleaningAllocation) = processCleaningDestination(data.cleaning_destination)
        
        val submissionData = mapOf(
            "property_id" to data.property_id,
            "platform" to data.platform,
            "reservation_code" to data.reservation_code,
            "guest_name" to data.guest_name,
            "guest_email" to data.guest_email,
            "guest_phone" to data.guest_phone,
            "number_of_guests" to data.number_of_guests,
            "check_in_date" to data.check_in_date,
            "check_out_date" to data.check_out_date,
            "checkin_time" to data.checkin_time,
            "checkout_time" to data.checkout_time,
            "total_revenue" to data.total_revenue.toBigDecimalOrNull(),
            "payment_status" to data.payment_status,
            "reservation_status" to data.reservation_status,
            "cleaner_user_id" to cleanerId,
            "cleaning_allocation" to cleaningAllocation,
            "cleaning_payment_status" to data.cleaning_payment_status,
            "cleaning_rating" to data.cleaning_rating,
            "cleaning_notes" to data.cleaning_notes,
            "cleaning_fee" to data.cleaning_fee?.toBigDecimalOrNull()
        )
        
        return try {
            val result = supabase.postgrest.from("reservations")
                .insert(submissionData)
                .decodeSingle<Reservation>()
            Result.success(result)
        } catch (e: Exception) {
            android.util.Log.e("ReservationRepo", "Erro ao criar reserva: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun updateReservation(id: String, data: com.riohhost.app.data.models.ReservationFormData): Result<Reservation> {
        val (cleanerId, cleaningAllocation) = processCleaningDestination(data.cleaning_destination)

        val submissionData = mapOf(
            "property_id" to data.property_id,
            "platform" to data.platform,
            "reservation_code" to data.reservation_code,
            "guest_name" to data.guest_name,
            "guest_email" to data.guest_email,
            "guest_phone" to data.guest_phone,
            "number_of_guests" to data.number_of_guests,
            "check_in_date" to data.check_in_date,
            "check_out_date" to data.check_out_date,
            "checkin_time" to data.checkin_time,
            "checkout_time" to data.checkout_time,
            "total_revenue" to data.total_revenue.toBigDecimalOrNull(),
            "payment_status" to data.payment_status,
            "reservation_status" to data.reservation_status,
            "cleaner_user_id" to cleanerId,
            "cleaning_allocation" to cleaningAllocation,
            "cleaning_payment_status" to data.cleaning_payment_status,
            "cleaning_rating" to data.cleaning_rating,
            "cleaning_notes" to data.cleaning_notes,
            "cleaning_fee" to data.cleaning_fee?.toBigDecimalOrNull()
        )

        return try {
            val result = supabase.postgrest.from("reservations")
                .update(submissionData) {
                    eq("id", id)
                }
                .decodeSingle<Reservation>()
            Result.success(result)
        } catch (e: Exception) {
            android.util.Log.e("ReservationRepo", "Erro ao atualizar reserva: ${e.message}")
            Result.failure(e)
        }
    }

    private fun processCleaningDestination(destination: String?): Pair<String?, String?> {
        val uuidRegex = "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$".toRegex(RegexOption.IGNORE_CASE)
        
        return when {
            destination?.matches(uuidRegex) == true -> {
                // É um UUID de faxineira
                Pair(destination, null)  // cleaner_user_id, cleaning_allocation
            }
            destination == "host" -> {
                Pair(null, "co_anfitriao")
            }
            destination == "owner" -> {
                Pair(null, "proprietario")
            }
            else -> {
                Pair(null, null)  // Nenhum
            }
        }
    }
}
