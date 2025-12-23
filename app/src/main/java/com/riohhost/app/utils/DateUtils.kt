package com.riohhost.app.utils

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Utility object for Brazilian date formatting and timezone handling.
 * All dates/times are based on America/Sao_Paulo (UTC-3).
 */
object DateUtils {
    
    // Brazilian timezone
    val SAO_PAULO_ZONE: ZoneId = ZoneId.of("America/Sao_Paulo")
    
    // Brazilian date format: dd/MM/yyyy
    private val BRAZILIAN_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale("pt", "BR"))
    
    // Brazilian datetime format: dd/MM/yyyy HH:mm
    private val BRAZILIAN_DATETIME_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale("pt", "BR"))
    
    // ISO date format for Supabase
    private val ISO_DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE
    
    /**
     * Get current date in São Paulo timezone.
     */
    fun nowInSaoPaulo(): LocalDate {
        return LocalDate.now(SAO_PAULO_ZONE)
    }
    
    /**
     * Get current datetime in São Paulo timezone.
     */
    fun nowDateTimeInSaoPaulo(): ZonedDateTime {
        return ZonedDateTime.now(SAO_PAULO_ZONE)
    }
    
    /**
     * Format LocalDate to Brazilian format (dd/MM/yyyy).
     */
    fun formatToBrazilian(date: LocalDate?): String {
        return date?.format(BRAZILIAN_DATE_FORMAT) ?: ""
    }
    
    /**
     * Format ISO date string (yyyy-MM-dd) to Brazilian format (dd/MM/yyyy).
     */
    fun formatIsoToBrazilian(isoDateString: String?): String {
        if (isoDateString.isNullOrBlank()) return ""
        return try {
            val date = LocalDate.parse(isoDateString.take(10), ISO_DATE_FORMAT)
            date.format(BRAZILIAN_DATE_FORMAT)
        } catch (e: Exception) {
            isoDateString
        }
    }
    
    /**
     * Format LocalDateTime to Brazilian format (dd/MM/yyyy HH:mm).
     */
    fun formatDateTimeToBrazilian(dateTime: LocalDateTime?): String {
        return dateTime?.format(BRAZILIAN_DATETIME_FORMAT) ?: ""
    }
    
    /**
     * Parse Brazilian date string (dd/MM/yyyy) to LocalDate.
     */
    fun parseBrazilianDate(dateString: String?): LocalDate? {
        if (dateString.isNullOrBlank()) return null
        return try {
            LocalDate.parse(dateString, BRAZILIAN_DATE_FORMAT)
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Parse ISO date string (yyyy-MM-dd) to LocalDate.
     */
    fun parseIsoDate(isoDateString: String?): LocalDate? {
        if (isoDateString.isNullOrBlank()) return null
        return try {
            LocalDate.parse(isoDateString.take(10), ISO_DATE_FORMAT)
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Format LocalDate to ISO string for Supabase queries.
     */
    fun formatToIso(date: LocalDate): String {
        return date.format(ISO_DATE_FORMAT)
    }
    
    /**
     * Compare two ISO date strings for sorting (descending order).
     * Returns negative if date1 > date2 (date1 comes first in descending).
     */
    fun compareIsoDateStringsDescending(date1: String?, date2: String?): Int {
        val d1 = parseIsoDate(date1)
        val d2 = parseIsoDate(date2)
        
        return when {
            d1 == null && d2 == null -> 0
            d1 == null -> 1
            d2 == null -> -1
            else -> d2.compareTo(d1) // Descending: larger dates first
        }
    }
}
