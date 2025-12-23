package com.riohhost.app.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
    @SerialName("commission_rate") val commissionRate: String? = null,
    @SerialName("cleaning_fee") val cleaningFee: String? = null,
    @SerialName("base_nightly_price") val baseNightlyPrice: String? = null,
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
    
    @SerialName("total_revenue") val totalRevenue: String? = null,
    @SerialName("base_revenue") val baseRevenue: String? = null,
    @SerialName("commission_amount") val commissionAmount: String? = null,
    @SerialName("net_revenue") val netRevenue: String? = null,
    @SerialName("cleaning_fee") val cleaningFee: String? = null,
    
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
