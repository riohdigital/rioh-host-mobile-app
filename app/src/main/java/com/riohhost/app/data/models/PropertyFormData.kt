package com.riohhost.app.data.models

import kotlinx.serialization.Serializable

@Serializable
data class PropertyFormData(
    // === OBRIGATÓRIOS ===
    val name: String,                     // "Rioh Host - Copacabana"
    val property_type: String,            // "Apartamento", "Casa", etc.
    val status: String = "Ativo",         // "Ativo", "Inativo", "Em Manutenção"
    val cleaning_fee: String,             // USAR STRING! Taxa de limpeza padrão
    
    // === OPCIONAIS ===
    val nickname: String? = null,         // "Apê Copa"
    val address: String? = null,          // Endereço completo
    val airbnb_link: String? = null,
    val booking_link: String? = null,
    val commission_rate: Int = 20,        // Percentual (20 = 20%)
    val base_nightly_price: String? = null, // USAR STRING!
    val max_guests: Int = 1,
    val notes: String? = null,
    val default_checkin_time: String = "15:00",  // Formato "HH:mm"
    val default_checkout_time: String = "11:00"  // Formato "HH:mm"
)

enum class PropertyType(val value: String) {
    APARTAMENTO("Apartamento"),
    CASA("Casa"),
    ESTUDIO("Estúdio"),
    LOFT("Loft"),
    KITNET("Kitnet")
}

enum class PropertyStatus(val value: String) {
    ATIVO("Ativo"),
    INATIVO("Inativo"),
    EM_MANUTENCAO("Em Manutenção")
}
