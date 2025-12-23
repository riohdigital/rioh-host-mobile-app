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

    suspend fun getReservationById(id: String): Reservation? {
        return try {
            supabase.postgrest.from("reservations")
                .select {
                    filter { eq("id", id) }
                }
                .decodeSingle<Reservation>()
        } catch (e: Exception) {
            android.util.Log.e("ReservationRepo", "ERRO ao buscar reserva por ID: ${e.message}", e)
            null
        }
    }

    suspend fun getCleanerReservations(cleanerId: String): List<Reservation> {
        return try {
            supabase.postgrest.rpc("fn_get_cleaner_reservations", mapOf("cleaner_id" to cleanerId))
                .decodeList<Reservation>()
        } catch (e: Exception) {
            android.util.Log.e("ReservationRepo", "ERRO ao buscar reservas do cleaner: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun getAvailableReservations(cleanerId: String): List<Reservation> {
        return try {
            supabase.postgrest.rpc("fn_get_available_reservations", mapOf("cleaner_id" to cleanerId))
                .decodeList<Reservation>()
        } catch (e: Exception) {
            android.util.Log.e("ReservationRepo", "ERRO ao buscar reservas disponíveis: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun assignCleaning(reservationId: String, cleanerId: String) {
        try {
            supabase.postgrest.rpc("assign_cleaning_with_permissions", 
                mapOf("reservation_id" to reservationId, "cleaner_id" to cleanerId))
        } catch (e: Exception) {
            android.util.Log.e("ReservationRepo", "ERRO ao atribuir limpeza: ${e.message}", e)
        }
    }

    suspend fun toggleCleaningStatus(reservationId: String) {
        try {
            supabase.postgrest.rpc("fn_toggle_cleaning_status", mapOf("reservation_id" to reservationId))
        } catch (e: Exception) {
            android.util.Log.e("ReservationRepo", "ERRO ao alternar status: ${e.message}", e)
        }
    }
}
