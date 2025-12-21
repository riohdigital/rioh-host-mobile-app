package com.riohhost.app.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val id: String, // likely UUID
    @SerialName("user_id") val userId: String, // Reference to auth.users
    val role: String, // master, owner, gestor, faxineira
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
    @SerialName("property_type") val propertyType: String? = null, // casa, apartamento, etc
    val status: String? = null, // ativo, inativo
    @SerialName("airbnb_link") val airbnbLink: String? = null,
    @SerialName("booking_link") val bookingLink: String? = null,
    @SerialName("commission_rate") val commissionRate: Double? = null,
    @SerialName("cleaning_fee") val cleaningFee: Double? = null,
    @SerialName("base_nightly_price") val baseNightlyPrice: Double? = null,
    @SerialName("max_guests") val maxGuests: Int? = null,
    val notes: String? = null
)

@Serializable
data class Reservation(
    val id: String,
    @SerialName("property_id") val propertyId: String,
    @SerialName("guest_name") val guestName: String? = null,
    @SerialName("guest_email") val guestEmail: String? = null,
    @SerialName("guest_phone") val guestPhone: String? = null,
    @SerialName("number_of_guests") val numberOfGuests: Int? = null,
    @SerialName("reservation_code") val reservationCode: String? = null,
    @SerialName("check_in_date") val checkInDate: String, // ISO date
    @SerialName("check_out_date") val checkOutDate: String, // ISO date
    @SerialName("cleaning_status") val cleaningStatus: String? = null,
    @SerialName("cleaner_user_id") val cleanerUserId: String? = null,
    @SerialName("cleaning_fee") val cleaningFee: Double? = null,
    @SerialName("reservation_status") val reservationStatus: String? = null,
    val platform: String? = null,
    @SerialName("total_revenue") val totalRevenue: Double? = null
)

@Serializable
data class Expense(
    val id: String,
    val description: String,
    val amount: Double,
    val category: String? = null,
    @SerialName("expense_date") val expenseDate: String
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
    val timestamp: String, // ISO Date
    val status: String? = null // sending, sent, error
)

@Serializable
data class NotificationDestination(
    val id: String,
    @SerialName("destination_name") val name: String,
    @SerialName("whatsapp_number") val whatsappNumber: String?,
    @SerialName("destination_role") val role: String,
    @SerialName("is_authenticated") val isAuthenticated: Boolean = false
)
