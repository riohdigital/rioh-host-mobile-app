package com.riohhost.app.data.models

import kotlinx.serialization.Serializable

@Serializable
data class ReservationWithCleanerInfo(
    val id: String,
    val property_id: String,
    val platform: String,
    val reservation_code: String,
    val guest_name: String?,
    val guest_phone: String?,
    val number_of_guests: Int?,
    val check_in_date: String,           // "2026-01-15"
    val check_out_date: String,          // "2026-01-18"
    val checkin_time: String?,           // "15:00"
    val checkout_time: String?,          // "11:00"
    val total_revenue: Double?,
    val base_revenue: Double?,
    val commission_amount: Double?,
    val net_revenue: Double?,
    val payment_status: String?,
    val reservation_status: String,      // "Confirmada", "Cancelada", "Finalizada"
    val is_communicated: Boolean?,
    val receipt_sent: Boolean?,
    val created_at: String,
    
    // Campos de Faxina (MAIS IMPORTANTES)
    val cleaner_user_id: String?,        // UUID da faxineira atribuida
    val cleaning_status: String?,        // "Pendente", "Realizada", "Em Andamento"
    val cleaning_payment_status: String?,// "Pagamento no Proximo Ciclo", "Pago"
    val cleaning_rating: Int?,           // 1-5 estrelas
    val cleaning_notes: String?,         // Notas sobre a faxina
    val cleaning_fee: Double?,           // Valor pago a faxineira
    val cleaning_allocation: String?,    // Alocacao de custos
    
    // Proximo check-in (para urgencia)
    val next_check_in_date: String?,
    val next_checkin_time: String?,
    
    // Objetos relacionados (retornados pela RPC)
    val properties: PropertyInfo?,
    val cleaner_info: CleaningCleanerInfo?
)

@Serializable
data class PropertyInfo(
    val id: String,
    val name: String,
    val address: String,
    val default_checkin_time: String?
)

@Serializable
data class CleaningCleanerInfo(
    val id: String, // Note: This might map to user_id or id depending on the view
    val full_name: String,
    val email: String,
    val phone: String?
)

@Serializable
data class CleaningCleanerProfile(
    val id: String,
    val user_id: String,
    val full_name: String,
    val email: String,
    val phone: String?,
    val is_active: Boolean?
)

data class CleanerStats(
    val name: String,
    val total: Int,
    val pending: Int,
    val completed: Int
)
