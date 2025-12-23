package com.riohhost.app.data.repositories

import com.riohhost.app.data.api.SupabaseClient
import com.riohhost.app.data.models.Reservation
import com.riohhost.app.data.models.ReservationCreate
import com.riohhost.app.data.models.ReservationUpdate
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
            result
        } catch (e: Exception) {
            android.util.Log.e("ReservationRepo", "ERRO ao buscar reservas: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Get reservations filtered by date range, properties, and platform.
     * Uses overlap logic: check_out >= startDate AND check_in <= endDate
     */
    suspend fun getReservationsFiltered(
        startDate: String,
        endDate: String,
        propertyIds: List<String>? = null,
        platform: String? = null
    ): List<Reservation> {
        return try {
            android.util.Log.d("ReservationRepo", "Buscando reservas: $startDate a $endDate")
            
            val result = supabase.postgrest.from("reservations")
                .select {
                    filter {
                        gte("check_out_date", startDate)
                        lte("check_in_date", endDate)
                        if (!platform.isNullOrEmpty() && platform != "all") {
                            eq("platform", platform)
                        }
                    }
                }
                .decodeList<Reservation>()
            
            // Property filter (applied client-side)
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
                .select { filter { eq("id", id) } }
                .decodeSingle<Reservation>()
        } catch (e: Exception) {
            android.util.Log.e("ReservationRepo", "ERRO ao buscar reserva por ID: ${e.message}", e)
            null
        }
    }

    suspend fun createReservation(reservation: ReservationCreate): Result<Reservation> {
        return try {
            android.util.Log.d("ReservationRepo", "Criando reserva: ${reservation.reservationCode}")
            val result = supabase.postgrest.from("reservations")
                .insert(reservation)
                .decodeSingle<Reservation>()
            android.util.Log.d("ReservationRepo", "Reserva criada: ${result.id}")
            Result.success(result)
        } catch (e: Exception) {
            android.util.Log.e("ReservationRepo", "ERRO ao criar reserva: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun updateReservation(id: String, updates: ReservationUpdate): Result<Reservation> {
        return try {
            android.util.Log.d("ReservationRepo", "Atualizando reserva: $id")
            val result = supabase.postgrest.from("reservations")
                .update(updates) {
                    filter { eq("id", id) }
                }
                .decodeSingle<Reservation>()
            android.util.Log.d("ReservationRepo", "Reserva atualizada: ${result.id}")
            Result.success(result)
        } catch (e: Exception) {
            android.util.Log.e("ReservationRepo", "ERRO ao atualizar reserva: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun deleteReservation(id: String): Result<Unit> {
        return try {
            android.util.Log.d("ReservationRepo", "Deletando reserva: $id")
            supabase.postgrest.from("reservations")
                .delete { filter { eq("id", id) } }
            android.util.Log.d("ReservationRepo", "Reserva deletada: $id")
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("ReservationRepo", "ERRO ao deletar reserva: ${e.message}", e)
            Result.failure(e)
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
            android.util.Log.e("ReservationRepo", "ERRO ao atribuir faxina: ${e.message}", e)
        }
    }

    suspend fun toggleCleaningStatus(reservationId: String) {
        try {
            supabase.postgrest.rpc("fn_toggle_cleaning_status", mapOf("reservation_id" to reservationId))
        } catch (e: Exception) {
            android.util.Log.e("ReservationRepo", "ERRO ao alterar status: ${e.message}", e)
        }
    }
}
