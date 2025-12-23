package com.riohhost.app.data.repositories

import com.riohhost.app.data.api.SupabaseClient
import com.riohhost.app.data.models.Expense
import io.github.jan.supabase.postgrest.postgrest

class ExpenseRepository {
    private val supabase = SupabaseClient.client

    /**
     * Get all expenses (optionally filtered by property).
     */
    suspend fun getExpenses(propertyId: String? = null): List<Expense> {
        return try {
            android.util.Log.d("ExpenseRepo", "Iniciando busca de despesas...")
            val result = supabase.postgrest.from("expenses")
                .select()
                .decodeList<Expense>()
            android.util.Log.d("ExpenseRepo", "Encontradas ${result.size} despesas")
            
            if (propertyId != null) {
                result.filter { it.propertyId == propertyId }
            } else {
                result
            }
        } catch (e: Exception) {
            android.util.Log.e("ExpenseRepo", "ERRO ao buscar despesas: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Get expenses filtered by date range and optionally by properties.
     * 
     * IMPORTANT: Server-side filtering with Supabase Kotlin SDK doesn't work reliably.
     * We fetch ALL expenses and filter CLIENT-SIDE by expense_date.
     * This matches what the web app does in useFinancialData.ts.
     */
    suspend fun getExpensesFiltered(
        startDate: String,
        endDate: String,
        propertyIds: List<String>? = null
    ): List<Expense> {
        return try {
            android.util.Log.d("ExpenseRepo", "Buscando despesas: $startDate a $endDate")
            
            // Fetch ALL expenses from database
            val allExpenses = supabase.postgrest.from("expenses")
                .select()
                .decodeList<Expense>()
            
            android.util.Log.d("ExpenseRepo", "Total de despesas no banco: ${allExpenses.size}")
            
            // CLIENT-SIDE filter by expense_date >= startDate AND expense_date <= endDate
            val dateFiltered = allExpenses.filter { expense ->
                val expenseDate = expense.expenseDate
                if (expenseDate == null) {
                    false
                } else {
                    // Compare as strings (ISO format works for string comparison)
                    expenseDate >= startDate && expenseDate <= endDate
                }
            }
            
            android.util.Log.d("ExpenseRepo", "Despesas apos filtro de data ($startDate a $endDate): ${dateFiltered.size}, total: R$ ${dateFiltered.sumOf { it.amount ?: 0.0 }}")
            
            // Property filter (also client-side)
            val finalResult = if (!propertyIds.isNullOrEmpty() && !propertyIds.contains("todas")) {
                dateFiltered.filter { expense -> 
                    expense.propertyId?.let { propertyIds.contains(it) } ?: false
                }
            } else {
                dateFiltered
            }
            
            android.util.Log.d("ExpenseRepo", "Despesas apos filtro de propriedades: ${finalResult.size}, total: R$ ${finalResult.sumOf { it.amount ?: 0.0 }}")
            finalResult
        } catch (e: Exception) {
            android.util.Log.e("ExpenseRepo", "ERRO ao buscar despesas filtradas: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Get expense by ID.
     */
    suspend fun getExpenseById(id: String): Expense? {
        return try {
            val allExpenses = supabase.postgrest.from("expenses")
                .select()
                .decodeList<Expense>()
            allExpenses.find { it.id == id }
        } catch (e: Exception) {
            android.util.Log.e("ExpenseRepo", "ERRO ao buscar despesa por ID: ${e.message}", e)
            null
        }
    }
}
