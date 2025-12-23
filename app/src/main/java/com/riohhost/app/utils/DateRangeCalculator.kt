package com.riohhost.app.utils

import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

/**
 * Utility object for calculating date ranges based on period codes.
 * Matches the logic from the web app's GlobalFiltersContext.
 */
object DateRangeCalculator {
    
    private val ISO_DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE
    
    /**
     * Period codes matching the web application.
     */
    object Periods {
        const val CURRENT_MONTH = "current_month"
        const val CURRENT_YEAR = "current_year"
        const val GENERAL = "general"
        const val CUSTOM = "custom"
        const val LAST_MONTH = "last_month"
        const val LAST_3_MONTHS = "last_3_months"
        const val LAST_6_MONTHS = "last_6_months"
        const val LAST_YEAR = "last_year"
        const val NEXT_MONTH = "next_month"
        const val NEXT_3_MONTHS = "next_3_months"
        const val NEXT_6_MONTHS = "next_6_months"
        const val NEXT_12_MONTHS = "next_12_months"
    }
    
    /**
     * Calculates the date range based on a period code.
     * Uses America/Sao_Paulo timezone for all calculations.
     * 
     * @param period Period code (e.g., "current_year", "last_month")
     * @param customStart Custom start date (only used when period == "custom")
     * @param customEnd Custom end date (only used when period == "custom")
     * @return Pair of (startDate, endDate) as LocalDate
     */
    fun calculate(
        period: String,
        customStart: LocalDate? = null,
        customEnd: LocalDate? = null
    ): Pair<LocalDate, LocalDate> {
        // Use São Paulo timezone
        val saoPauloZone = java.time.ZoneId.of("America/Sao_Paulo")
        val today = LocalDate.now(saoPauloZone)
        
        return when (period) {
            Periods.CURRENT_MONTH -> {
                val yearMonth = YearMonth.now(saoPauloZone)
                Pair(yearMonth.atDay(1), yearMonth.atEndOfMonth())
            }
            
            Periods.CURRENT_YEAR -> {
                val year = today.year
                Pair(LocalDate.of(year, 1, 1), LocalDate.of(year, 12, 31))
            }
            
            Periods.GENERAL -> {
                // All historical data (1900-2099)
                Pair(LocalDate.of(1900, 1, 1), LocalDate.of(2099, 12, 31))
            }
            
            Periods.CUSTOM -> {
                // Use provided custom dates or fallback to current year
                Pair(
                    customStart ?: LocalDate.of(today.year, 1, 1),
                    customEnd ?: LocalDate.of(today.year, 12, 31)
                )
            }
            
            Periods.LAST_MONTH -> {
                val lastMonth = YearMonth.now(saoPauloZone).minusMonths(1)
                Pair(lastMonth.atDay(1), lastMonth.atEndOfMonth())
            }
            
            Periods.LAST_3_MONTHS -> {
                val endDate = today
                val startDate = today.minusMonths(3)
                Pair(startDate, endDate)
            }
            
            Periods.LAST_6_MONTHS -> {
                val endDate = today
                val startDate = today.minusMonths(6)
                Pair(startDate, endDate)
            }
            
            Periods.LAST_YEAR -> {
                val lastYear = today.year - 1
                Pair(LocalDate.of(lastYear, 1, 1), LocalDate.of(lastYear, 12, 31))
            }
            
            Periods.NEXT_MONTH -> {
                val nextMonth = YearMonth.now(saoPauloZone).plusMonths(1)
                Pair(nextMonth.atDay(1), nextMonth.atEndOfMonth())
            }
            
            Periods.NEXT_3_MONTHS -> {
                val startDate = today
                val endDate = today.plusMonths(3)
                Pair(startDate, endDate)
            }
            
            Periods.NEXT_6_MONTHS -> {
                val startDate = today
                val endDate = today.plusMonths(6)
                Pair(startDate, endDate)
            }
            
            Periods.NEXT_12_MONTHS -> {
                val startDate = today
                val endDate = today.plusMonths(12)
                Pair(startDate, endDate)
            }
            
            else -> {
                // Default to current year
                val year = today.year
                Pair(LocalDate.of(year, 1, 1), LocalDate.of(year, 12, 31))
            }
        }
    }
    
    /**
     * Formats a LocalDate to ISO string (YYYY-MM-DD) for Supabase queries.
     */
    fun toIsoString(date: LocalDate): String {
        return date.format(ISO_DATE_FORMAT)
    }
    
    /**
     * Parses an ISO date string to LocalDate.
     */
    fun fromIsoString(dateString: String): LocalDate? {
        return try {
            LocalDate.parse(dateString, ISO_DATE_FORMAT)
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Returns a list of all available periods for UI dropdowns.
     */
    fun getAllPeriods(): List<Pair<String, String>> {
        return listOf(
            Periods.CURRENT_MONTH to "Mês Atual",
            Periods.CURRENT_YEAR to "Ano Atual",
            Periods.LAST_MONTH to "Mês Passado",
            Periods.LAST_3_MONTHS to "Últimos 3 Meses",
            Periods.LAST_6_MONTHS to "Últimos 6 Meses",
            Periods.LAST_YEAR to "Ano Passado",
            Periods.NEXT_MONTH to "Próximo Mês",
            Periods.NEXT_3_MONTHS to "Próximos 3 Meses",
            Periods.NEXT_6_MONTHS to "Próximos 6 Meses",
            Periods.NEXT_12_MONTHS to "Próximos 12 Meses",
            Periods.GENERAL to "Todo Período",
            Periods.CUSTOM to "Personalizado"
        )
    }
}
