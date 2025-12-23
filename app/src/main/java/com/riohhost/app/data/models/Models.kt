package com.riohhost.app.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ===== Enums =====

enum class ReservationStatus(val displayName: String) {
    CONFIRMADA("Confirmada"),
    EM_ANDAMENTO("Em Andamento"),
    FINALIZADA("Finalizada"),
    CANCELADA("Cancelada");
    
    companion object {
        fun fromString(value: String?): ReservationStatus? {
            return entries.find { it.name.equals(value, ignoreCase = true) || it.displayName.equals(value, ignoreCase = true) }
        }
    }
}

enum class PaymentStatus(val displayName: String) {
    PAGO("Pago"),
    PENDENTE("Pendente"),
    ATRASADO("Atrasado");
    
    companion object {
        fun fromString(value: String?): PaymentStatus? {
            return entries.find { it.name.equals(value, ignoreCase = true) || it.displayName.equals(value, ignoreCase = true) }
        }
    }
}

enum class Platform(val displayName: String) {
    AIRBNB("Airbnb"),
    BOOKING("Booking.com"),
    DIRETO("Direto");
    
    companion object {
        fun fromString(value: String?): Platform? {
            return entries.find { 
                it.name.equals(value, ignoreCase = true) || 
                it.displayName.equals(value, ignoreCase = true) ||
                (it == BOOKING && value?.lowercase()?.contains("booking") == true)
            }
        }
    }
}

enum class CleaningAllocation(val displayName: String) {
    CO_ANFITRIAO("Co-anfitrião"),
    PROPRIETARIO("Proprietário"),
    FAXINEIRA("Faxineira");
    
    companion object {
        fun fromString(value: String?): CleaningAllocation? {
            return entries.find { it.name.equals(value?.replace("-", "_"), ignoreCase = true) || it.displayName.equals(value, ignoreCase = true) }
        }
    }
}

enum class PropertyType(val displayName: String) {
    APARTAMENTO("Apartamento"),
    CASA("Casa"),
    STUDIO("Studio"),
    FLAT("Flat"),
    QUARTO("Quarto");
    
    companion object {
        fun fromString(value: String?): PropertyType? {
            return entries.find { it.name.equals(value, ignoreCase = true) || it.displayName.equals(value, ignoreCase = true) }
        }
    }
}

enum class PropertyStatus(val displayName: String) {
    ATIVO("Ativo"),
    INATIVO("Inativo"),
    MANUTENCAO("Manutenção");
    
    companion object {
        fun fromString(value: String?): PropertyStatus? {
            return entries.find { it.name.equals(value, ignoreCase = true) || it.displayName.equals(value, ignoreCase = true) }
        }
    }
}

// ===== Data Classes =====

@Serializable
data class UserProfile(
    val id: String,
    @SerialName("user_id") val userId: String,
    val role: String,
    @SerialName("full_name") val fullName: String? = null,
    val email: String? = null,
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class Property(
    val id: String,
    val name: String,
    val nickname: String? = null,
    val address: String? = null,
    @SerialName("property_type") val propertyType: String? = null,
    val status: String? = null,
    @SerialName("airbnb_link") val airbnbLink: String? = null,
    @SerialName("booking_link") val bookingLink: String? = null,
    @SerialName("commission_rate") val commissionRate: Double? = null,
    @SerialName("cleaning_fee") val cleaningFee: Double? = null,
    @SerialName("base_nightly_price") val baseNightlyPrice: Double? = null,
    @SerialName("max_guests") val maxGuests: Int? = null,
    val notes: String? = null,
    @SerialName("default_checkin_time") val defaultCheckinTime: String? = null,
    @SerialName("default_checkout_time") val defaultCheckoutTime: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("created_by") val createdBy: String? = null
)

@Serializable
data class Reservation(
    val id: String,
    @SerialName("property_id") val propertyId: String? = null,
    val platform: String? = null,
    @SerialName("reservation_code") val reservationCode: String? = null,
    @SerialName("check_in_date") val checkInDate: String? = null,
    @SerialName("check_out_date") val checkOutDate: String? = null,
    @SerialName("guest_name") val guestName: String? = null,
    @SerialName("guest_phone") val guestPhone: String? = null,
    @SerialName("guest_email") val guestEmail: String? = null,
    @SerialName("number_of_guests") val numberOfGuests: Int? = null,
    
    @SerialName("total_revenue") val totalRevenue: Double? = null,
    @SerialName("base_revenue") val baseRevenue: Double? = null,
    @SerialName("commission_amount") val commissionAmount: Double? = null,
    @SerialName("net_revenue") val netRevenue: Double? = null,
    @SerialName("cleaning_fee") val cleaningFee: Double? = null,
    
    @SerialName("reservation_status") val reservationStatus: String? = null,
    @SerialName("payment_status") val paymentStatus: String? = null,
    @SerialName("payment_date") val paymentDate: String? = null,
    
    @SerialName("checkin_time") val checkinTime: String? = null,
    @SerialName("checkout_time") val checkoutTime: String? = null,
    
    @SerialName("cleaner_user_id") val cleanerUserId: String? = null,
    @SerialName("cleaning_status") val cleaningStatus: String? = null,
    @SerialName("cleaning_payment_status") val cleaningPaymentStatus: String? = null,
    @SerialName("cleaning_rating") val cleaningRating: Int? = null,
    @SerialName("cleaning_notes") val cleaningNotes: String? = null,
    @SerialName("cleaning_allocation") val cleaningAllocation: String? = null,
    
    @SerialName("is_communicated") val isCommunicated: Boolean? = false,
    @SerialName("receipt_sent") val receiptSent: Boolean? = false,
    
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("created_by") val createdBy: String? = null,
    @SerialName("created_by_source") val createdBySource: String? = null,
    @SerialName("automation_metadata") val automationMetadata: String? = null
)

// For creating/updating reservations (only fields we send)
@Serializable
data class ReservationCreate(
    @SerialName("property_id") val propertyId: String,
    val platform: String,
    @SerialName("reservation_code") val reservationCode: String,
    @SerialName("check_in_date") val checkInDate: String,
    @SerialName("check_out_date") val checkOutDate: String,
    @SerialName("guest_name") val guestName: String? = null,
    @SerialName("guest_phone") val guestPhone: String? = null,
    @SerialName("guest_email") val guestEmail: String? = null,
    @SerialName("number_of_guests") val numberOfGuests: Int? = null,
    @SerialName("total_revenue") val totalRevenue: Double,
    @SerialName("checkin_time") val checkinTime: String? = null,
    @SerialName("checkout_time") val checkoutTime: String? = null,
    @SerialName("reservation_status") val reservationStatus: String,
    @SerialName("payment_status") val paymentStatus: String? = null,
    @SerialName("cleaning_allocation") val cleaningAllocation: String? = null,
    @SerialName("cleaner_user_id") val cleanerUserId: String? = null,
    @SerialName("created_by_source") val createdBySource: String = "android_app"
)

@Serializable
data class ReservationUpdate(
    @SerialName("property_id") val propertyId: String? = null,
    val platform: String? = null,
    @SerialName("reservation_code") val reservationCode: String? = null,
    @SerialName("check_in_date") val checkInDate: String? = null,
    @SerialName("check_out_date") val checkOutDate: String? = null,
    @SerialName("guest_name") val guestName: String? = null,
    @SerialName("guest_phone") val guestPhone: String? = null,
    @SerialName("guest_email") val guestEmail: String? = null,
    @SerialName("number_of_guests") val numberOfGuests: Int? = null,
    @SerialName("total_revenue") val totalRevenue: Double? = null,
    @SerialName("checkin_time") val checkinTime: String? = null,
    @SerialName("checkout_time") val checkoutTime: String? = null,
    @SerialName("reservation_status") val reservationStatus: String? = null,
    @SerialName("payment_status") val paymentStatus: String? = null,
    @SerialName("cleaning_allocation") val cleaningAllocation: String? = null,
    @SerialName("cleaner_user_id") val cleanerUserId: String? = null
)

// For creating/updating properties
@Serializable
data class PropertyCreate(
    val name: String,
    val nickname: String? = null,
    val address: String? = null,
    @SerialName("property_type") val propertyType: String,
    val status: String = "Ativo",
    @SerialName("airbnb_link") val airbnbLink: String? = null,
    @SerialName("booking_link") val bookingLink: String? = null,
    @SerialName("commission_rate") val commissionRate: Double = 0.20,
    @SerialName("cleaning_fee") val cleaningFee: Double? = null,
    @SerialName("base_nightly_price") val baseNightlyPrice: Double? = null,
    @SerialName("max_guests") val maxGuests: Int? = null,
    @SerialName("default_checkin_time") val defaultCheckinTime: String = "15:00",
    @SerialName("default_checkout_time") val defaultCheckoutTime: String = "11:00",
    val notes: String? = null
)

@Serializable
data class PropertyUpdate(
    val name: String? = null,
    val nickname: String? = null,
    val address: String? = null,
    @SerialName("property_type") val propertyType: String? = null,
    val status: String? = null,
    @SerialName("airbnb_link") val airbnbLink: String? = null,
    @SerialName("booking_link") val bookingLink: String? = null,
    @SerialName("commission_rate") val commissionRate: Double? = null,
    @SerialName("cleaning_fee") val cleaningFee: Double? = null,
    @SerialName("base_nightly_price") val baseNightlyPrice: Double? = null,
    @SerialName("max_guests") val maxGuests: Int? = null,
    @SerialName("default_checkin_time") val defaultCheckinTime: String? = null,
    @SerialName("default_checkout_time") val defaultCheckoutTime: String? = null,
    val notes: String? = null
)

@Serializable
data class Expense(
    val id: String,
    @SerialName("property_id") val propertyId: String? = null,
    val description: String? = null,
    val amount: Double? = null,
    val category: String? = null,
    @SerialName("expense_date") val expenseDate: String? = null
)

@Serializable
enum class MessageRole {
    @SerialName("user") USER,
    @SerialName("assistant") ASSISTANT,
    @SerialName("system") SYSTEM
}

@Serializable
data class ChatMessage(
    val id: String,
    val role: MessageRole,
    val content: String,
    val timestamp: String,
    val status: String? = null
)

@Serializable
data class NotificationDestination(
    val id: String,
    val name: String,
    @SerialName("whatsapp_number") val whatsappNumber: String? = null,
    val role: String? = null,
    @SerialName("is_authenticated") val isAuthenticated: Boolean = false
)

// Cleaning Management Models
@Serializable
data class CleanerProfile(
    val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("full_name") val fullName: String,
    val email: String? = null,
    val phone: String? = null,
    @SerialName("is_active") val isActive: Boolean = true
)

@Serializable
data class CleaningReservation(
    val id: String,
    @SerialName("property_id") val propertyId: String,
    val platform: String? = null,
    @SerialName("reservation_code") val reservationCode: String? = null,
    @SerialName("check_in_date") val checkInDate: String? = null,
    @SerialName("check_out_date") val checkOutDate: String? = null,
    @SerialName("checkin_time") val checkinTime: String? = null,
    @SerialName("checkout_time") val checkoutTime: String? = null,
    @SerialName("guest_name") val guestName: String? = null,
    @SerialName("number_of_guests") val numberOfGuests: Int? = null,
    
    @SerialName("cleaner_user_id") val cleanerUserId: String? = null,
    @SerialName("cleaning_status") val cleaningStatus: String? = null,
    @SerialName("cleaning_payment_status") val cleaningPaymentStatus: String? = null,
    @SerialName("cleaning_fee") val cleaningFee: Double? = null,
    @SerialName("cleaning_rating") val cleaningRating: Int? = null,
    @SerialName("cleaning_notes") val cleaningNotes: String? = null,
    
    @SerialName("next_check_in_date") val nextCheckInDate: String? = null,
    @SerialName("next_checkin_time") val nextCheckinTime: String? = null,
    
    val properties: PropertyInfo? = null,
    @SerialName("cleaner_info") val cleanerInfo: CleanerInfo? = null
)

@Serializable
data class PropertyInfo(
    val id: String,
    val name: String,
    val address: String? = null,
    @SerialName("default_checkin_time") val defaultCheckinTime: String? = null
)

@Serializable
data class CleanerInfo(
    val id: String,
    @SerialName("full_name") val fullName: String,
    val email: String? = null,
    val phone: String? = null
)
