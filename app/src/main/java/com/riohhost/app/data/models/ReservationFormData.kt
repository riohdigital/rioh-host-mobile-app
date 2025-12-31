package com.riohhost.app.data.models

import kotlinx.serialization.Serializable

@Serializable
data class ReservationFormData(
    // === OBRIGATÓRIOS ===
    val platform: String,           // "Airbnb", "Booking.com", "Direto", "Outros"
    val reservation_code: String,   // Ex: "HMAB123456"
    val check_in_date: String,      // Formato: "yyyy-MM-dd"
    val check_out_date: String,     // Formato: "yyyy-MM-dd" (deve ser > check_in_date)
    val total_revenue: String,      // IMPORTANTE: Usar String, converter para BigDecimal
    val reservation_status: String, // "Confirmada", "Em Andamento", "Finalizada", "Cancelada"
    
    // === OPCIONAIS ===
    val property_id: String? = null,
    val guest_name: String? = null,
    val guest_email: String? = null,
    val guest_phone: String? = null,
    val number_of_guests: Int? = null,
    val checkin_time: String? = null,   // Formato: "HH:mm"
    val checkout_time: String? = null,  // Formato: "HH:mm"
    val payment_status: String? = "Pendente", // "Pendente", "Pago", "Atrasado"
    
    // === CAMPOS DE FAXINA ===
    val cleaner_user_id: String? = null,
    val cleaning_destination: String? = null, // "none", "host", "owner", ou UUID da faxineira
    val cleaning_payment_status: String? = "Pagamento no Próximo Ciclo",
    val cleaning_rating: Int? = 0,  // 0-5
    val cleaning_notes: String? = null,
    val cleaning_fee: String? = null,  // USAR STRING!
)

enum class Platform(val value: String) {
    AIRBNB("Airbnb"),
    BOOKING("Booking.com"),
    DIRETO("Direto"),
    OUTROS("Outros")
}

enum class ReservationStatus(val value: String) {
    CONFIRMADA("Confirmada"),
    EM_ANDAMENTO("Em Andamento"),
    FINALIZADA("Finalizada"),
    CANCELADA("Cancelada")
}

enum class PaymentStatus(val value: String) {
    PENDENTE("Pendente"),
    PAGO("Pago"),
    ATRASADO("Atrasado")
}

enum class CleaningPaymentStatus(val value: String) {
    PAGA("Paga"),
    PROXIMO_CICLO("Pagamento no Próximo Ciclo")
}
