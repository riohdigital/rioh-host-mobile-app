package com.riohhost.app.utils

import java.text.NumberFormat
import java.util.Locale

object CurrencyUtils {
    fun formatBRL(amount: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
        return format.format(amount)
    }
    
    /**
     * Format currency in compact form (e.g., R$ 14,5K for R$ 14.500,00)
     */
    fun formatBRLCompact(amount: Double): String {
        return when {
            amount >= 1_000_000 -> String.format("R$ %.1fM", amount / 1_000_000)
            amount >= 1_000 -> String.format("R$ %.1fK", amount / 1_000)
            else -> formatBRL(amount)
        }
    }
}
