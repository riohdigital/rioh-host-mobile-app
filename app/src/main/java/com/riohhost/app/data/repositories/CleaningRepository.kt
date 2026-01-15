package com.riohhost.app.data.repositories

import com.riohhost.app.data.api.SupabaseClient
import com.riohhost.app.data.models.CleaningCleanerProfile
import com.riohhost.app.data.models.ReservationWithCleanerInfo
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.add

class CleaningRepository {
    private val supabase = SupabaseClient.client

    suspend fun getAllCleanerReservations(
        startDate: String,
        endDate: String,
        propertyIds: List<String>?
    ): List<ReservationWithCleanerInfo> {
        return supabase.postgrest.rpc(
            "fn_get_all_cleaner_reservations",
            buildJsonObject {
                put("start_date", startDate)
                put("end_date", endDate)
                putJsonArray("property_ids") {
                    propertyIds?.forEach { add(it) }
                }
            }
        ).decodeList().also { println("DEBUG: getAllCleanerReservations result size: ${it.size}") }
    }

    suspend fun getAllAvailableReservations(
        startDate: String,
        endDate: String,
        propertyIds: List<String>?
    ): List<ReservationWithCleanerInfo> {
        return supabase.postgrest.rpc(
            "fn_get_all_available_reservations",
            buildJsonObject {
                put("start_date", startDate)
                put("end_date", endDate)
                putJsonArray("property_ids") {
                    propertyIds?.forEach { add(it) }
                }
            }
        ).decodeList().also { println("DEBUG: getAllAvailableReservations result size: ${it.size}") }
    }

    suspend fun getCleanersForProperties(
        propertyIds: List<String>?
    ): List<CleaningCleanerProfile> {
        return supabase.postgrest.rpc(
            "fn_get_cleaners_for_properties",
            buildJsonObject {
                putJsonArray("property_ids") {
                    propertyIds?.forEach { add(it) }
                }
            }
        ).decodeList()
    }

    suspend fun getPropertyCleanersForUser(
        propertyId: String
    ): List<CleaningCleanerProfile> {
        return try {
            supabase.postgrest.rpc(
                "fn_get_property_cleaners_for_user",
                mapOf("p_property_id" to propertyId)
            ).decodeList()
        } catch (e: Exception) {
            println("CleaningRepo: Error fetching cleaners for user property: ${e.message}")
            emptyList()
        }
    }

    suspend fun assignCleaning(
        reservationId: String,
        cleanerId: String
    ): Result<Unit> {
        return try {
            supabase.postgrest.rpc(
                "assign_cleaning_with_permissions",
                mapOf(
                    "reservation_id" to reservationId,
                    "cleaner_id" to cleanerId
                )
            )
            Result.success(Unit)
        } catch (e: Exception) {
            println("CleaningRepo: Error assigning cleaning: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun reassignCleaning(
        reservationId: String,
        newCleanerId: String
    ): Result<Unit> {
        return try {
            supabase.postgrest.rpc(
                "reassign_cleaning_with_permissions",
                mapOf(
                    "reservation_id" to reservationId,
                    "new_cleaner_id" to newCleanerId
                )
            )
            Result.success(Unit)
        } catch (e: Exception) {
            println("CleaningRepo: Error reassigning cleaning: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun unassignCleaning(
        reservationId: String
    ): Result<Unit> {
        return try {
            supabase.postgrest.rpc(
                "unassign_cleaning_with_permissions",
                mapOf("reservation_id" to reservationId)
            )
            Result.success(Unit)
        } catch (e: Exception) {
            println("CleaningRepo: Error unassigning cleaning: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun toggleCleaningStatus(
        reservationId: String
    ): Result<String> {
        return try {
            val result = supabase.postgrest.rpc(
                "fn_toggle_cleaning_status",
                mapOf("p_reservation_id" to reservationId)
            ).decodeAs<String>()
            Result.success(result)
        } catch (e: Exception) {
            println("CleaningRepo: Error toggling status: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun hasPermission(permissionType: String): Boolean {
        return try {
            val userId = supabase.auth.currentUserOrNull()?.id ?: return false
            
            val result = supabase.postgrest
                .from("user_permissions")
                .select {
                    filter {
                        eq("user_id", userId)
                        eq("permission_type", permissionType)
                        eq("permission_value", true)
                    }
                }
                .decodeSingleOrNull<JsonObject>()
            
            result != null
        } catch (e: Exception) {
            println("CleaningRepo: Permission check failed: ${e.message}")
            false
        }
    }
}
