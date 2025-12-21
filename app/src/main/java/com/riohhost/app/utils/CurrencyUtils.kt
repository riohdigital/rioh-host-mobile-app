package com.riohhost.app.utils

import java.text.NumberFormat
import java.util.Locale

object CurrencyUtils {
    fun formatBRL(amount: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
        return format.format(amount)
    }
}
