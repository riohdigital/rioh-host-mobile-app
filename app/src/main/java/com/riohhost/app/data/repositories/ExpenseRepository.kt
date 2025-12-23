package com.riohhost.app.data.repositories

import com.riohhost.app.data.api.SupabaseClient
import com.riohhost.app.data.models.Expense
import io.github.jan.supabase.postgrest.postgrest

class ExpenseRepository {
    private val supabase = SupabaseClient.client

    suspend fun getExpenses(propertyId: String? = null): List<Expense> {
        return try {
            android.util.Log.d("ExpenseRepo", "Iniciando busca de despesas...")
            val result = supabase.postgrest.from("expenses")
                .select {
                    if (propertyId != null) {
                        filter { eq("property_id", propertyId) }
                    }
                }
                .decodeList<Expense>()
            android.util.Log.d("ExpenseRepo", "Encontradas ${result.size} despesas")
            result
        } catch (e: Exception) {
            android.util.Log.e("ExpenseRepo", "ERRO ao buscar despesas: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun getExpensesFiltered(
        startDate: String,
        endDate: String,
        propertyIds: List<String>? = null
    ): List<Expense> {
        return try {
            android.util.Log.d("ExpenseRepo", "Buscando despesas: $startDate a $endDate")
            
            val result = supabase.postgrest.from("expenses")
                .select {
                    filter {
                        gte("expense_date", startDate)
                        lte("expense_date", endDate)
                    }
                }
                .decodeList<Expense>()
            
            val filteredResult = if (!propertyIds.isNullOrEmpty() && !propertyIds.contains("todas")) {
                result.filter { expense -> 
                    expense.propertyId?.let { propertyIds.contains(it) } ?: false
                }
            } else {
                result
            }
            
            android.util.Log.d("ExpenseRepo", "Encontradas ${filteredResult.size} despesas filtradas")
            filteredResult
        } catch (e: Exception) {
            android.util.Log.e("ExpenseRepo", "ERRO ao buscar despesas filtradas: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun getExpenseById(id: String): Expense? {
        return try {
            supabase.postgrest.from("expenses")
                .select { filter { eq("id", id) } }
                .decodeSingle<Expense>()
        } catch (e: Exception) {
            android.util.Log.e("ExpenseRepo", "ERRO ao buscar despesa por ID: ${e.message}", e)
            null
        }
    }
}
