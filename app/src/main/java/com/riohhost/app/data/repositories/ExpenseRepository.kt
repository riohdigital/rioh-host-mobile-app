package com.riohhost.app.data.repositories

import com.riohhost.app.data.api.SupabaseClient
import com.riohhost.app.data.models.Expense
import io.github.jan.supabase.postgrest.postgrest

class ExpenseRepository {
    private val supabase = SupabaseClient.client

    suspend fun getExpenses(propertyId: String? = null): List<Expense> {
        return try {
            supabase.postgrest.from("expenses")
                .select {
                    if (propertyId != null) {
                        filter { eq("property_id", propertyId) }
                    }
                }
                .decodeList<Expense>()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
