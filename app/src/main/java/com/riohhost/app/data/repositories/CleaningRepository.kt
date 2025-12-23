package com.riohhost.app.data.repositories

import com.riohhost.app.data.api.SupabaseClient
import com.riohhost.app.data.models.CleanerProfile
import com.riohhost.app.data.models.CleaningReservation
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc

class CleaningRepository {
    private val supabase = SupabaseClient.client

    /**
     * Busca todas as faxinas com cleaner atribuído
     * Requer role: master, owner, ou permissão gestao_faxinas_view
     */
    suspend fun getAllCleanerReservations(
        startDate: String? = null,
        endDate: String? = null,
        propertyIds: List<String>? = null
    ): Result<List<CleaningReservation>> {
        return try {
            val params = buildMap<String, Any> {
                startDate?.let { put("start_date", it) }
                endDate?.let { put("end_date", it) }
                propertyIds?.let { put("property_ids", it) }
            }
            
            val result = supabase.postgrest.rpc(
                "fn_get_all_cleaner_reservations",
                params
            ).decodeList<CleaningReservation>()
            
            android.util.Log.d("CleaningRepo", "Encontradas ${result.size} faxinas atribuídas")
            Result.success(result)
        } catch (e: Exception) {
            android.util.Log.e("CleaningRepo", "Erro ao buscar faxinas: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Busca faxinas disponíveis (sem cleaner atribuído)
     */
    suspend fun getAvailableReservations(
        startDate: String? = null,
        endDate: String? = null,
        propertyIds: List<String>? = null
    ): Result<List<CleaningReservation>> {
        return try {
            val params = buildMap<String, Any> {
                startDate?.let { put("start_date", it) }
                endDate?.let { put("end_date", it) }
                propertyIds?.let { put("property_ids", it) }
            }
            
            val result = supabase.postgrest.rpc(
                "fn_get_all_available_reservations",
                params
            ).decodeList<CleaningReservation>()
            
            android.util.Log.d("CleaningRepo", "Encontradas ${result.size} faxinas disponíveis")
            Result.success(result)
        } catch (e: Exception) {
            android.util.Log.e("CleaningRepo", "Erro ao buscar faxinas disponíveis: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Busca faxineiras disponíveis para as propriedades
     */
    suspend fun getCleanersForProperties(
        propertyIds: List<String>? = null
    ): Result<List<CleanerProfile>> {
        return try {
            val params = propertyIds?.let { mapOf("property_ids" to it) } ?: emptyMap<String, Any>()
            
            val result = supabase.postgrest.rpc(
                "fn_get_cleaners_for_properties",
                params
            ).decodeList<CleanerProfile>()
            
            android.util.Log.d("CleaningRepo", "Encontradas ${result.size} faxineiras")
            Result.success(result)
        } catch (e: Exception) {
            android.util.Log.e("CleaningRepo", "Erro ao buscar faxineiras: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Atribui faxineira a uma reserva
     */
    suspend fun assignCleaning(
        reservationId: String,
        cleanerId: String
    ): Result<String> {
        return try {
            val result = supabase.postgrest.rpc(
                "assign_cleaning_with_permissions",
                mapOf(
                    "reservation_id" to reservationId,
                    "cleaner_id" to cleanerId
                )
            ).decodeAs<String>()
            
            android.util.Log.d("CleaningRepo", "Faxina atribuída: $result")
            Result.success(result)
        } catch (e: Exception) {
            android.util.Log.e("CleaningRepo", "Erro ao atribuir faxina: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Reatribui faxineira (muda de uma para outra)
     */
    suspend fun reassignCleaning(
        reservationId: String,
        newCleanerId: String
    ): Result<String> {
        return try {
            val result = supabase.postgrest.rpc(
                "reassign_cleaning_with_permissions",
                mapOf(
                    "reservation_id" to reservationId,
                    "new_cleaner_id" to newCleanerId
                )
            ).decodeAs<String>()
            
            android.util.Log.d("CleaningRepo", "Faxina reatribuída: $result")
            Result.success(result)
        } catch (e: Exception) {
            android.util.Log.e("CleaningRepo", "Erro ao reatribuir faxina: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Remove faxineira de uma reserva
     */
    suspend fun unassignCleaning(reservationId: String): Result<String> {
        return try {
            val result = supabase.postgrest.rpc(
                "unassign_cleaning_with_permissions",
                mapOf("reservation_id" to reservationId)
            ).decodeAs<String>()
            
            android.util.Log.d("CleaningRepo", "Faxineira removida: $result")
            Result.success(result)
        } catch (e: Exception) {
            android.util.Log.e("CleaningRepo", "Erro ao remover faxineira: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Alterna status: Pendente ↔ Realizada
     */
    suspend fun toggleCleaningStatus(reservationId: String): Result<String> {
        return try {
            val result = supabase.postgrest.rpc(
                "fn_toggle_cleaning_status",
                mapOf("p_reservation_id" to reservationId)
            ).decodeAs<String>()
            
            android.util.Log.d("CleaningRepo", "Status alterado: $result")
            Result.success(result)
        } catch (e: Exception) {
            android.util.Log.e("CleaningRepo", "Erro ao alterar status: ${e.message}", e)
            Result.failure(e)
        }
    }
}
