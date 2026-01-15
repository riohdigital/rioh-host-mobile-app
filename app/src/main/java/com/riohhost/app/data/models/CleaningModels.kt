package com.riohhost.app.data.models

import kotlinx.serialization.Serializable

@Serializable
data class ReservationWithCleanerInfo(
    val id: String,
    val property_id: String? = null,
    val platform: String? = null,
    val reservation_code: String? = null,
    val check_in_date: String? = null,
    val check_out_date: String? = null,
    val payment_date: String? = null,
    val total_revenue: Double? = null,
    val payment_status: String? = null,
    val reservation_status: String? = null,
    val created_at: String? = null,
    val guest_name: String? = null,
    val number_of_guests: Int? = null,
    val base_revenue: Double? = null,
    val commission_amount: Double? = null,
    val net_revenue: Double? = null,
    val checkin_time: String? = null,
    val checkout_time: String? = null,
    val is_communicated: Boolean? = null,
    val receipt_sent: Boolean? = null,
    val guest_phone: String? = null,
    val cleaner_user_id: String? = null,
    val cleaning_payment_status: String? = null,
    val cleaning_rating: Int? = null,
    val cleaning_notes: String? = null,
    val cleaning_fee: Double? = null,
    val cleaning_allocation: String? = null,
    val cleaning_status: String? = null,
    val next_check_in_date: String? = null,
    val next_checkin_time: String? = null,
    
    val properties: PropertyInfo? = null,
    val cleaner_info: CleaningCleanerInfo? = null
)

@Serializable
data class PropertyInfo(
    val id: String? = null,
    val name: String? = null,
    val address: String? = null,
    val nickname: String? = null,
    val default_checkin_time: String? = null,
    val default_checkout_time: String? = null
)

@Serializable
data class CleaningCleanerInfo(
    val id: String? = null, 
    val full_name: String? = null,
    val email: String? = null,
    val phone: String? = null
)

@Serializable
data class CleaningCleanerProfile(
    val id: String? = null,
    val user_id: String? = null,
    val full_name: String? = null,
    val email: String? = null,
    val phone: String?,
    val is_active: Boolean?
)

data class CleanerStats(
    val name: String,
    val total: Int,
    val pending: Int,
    val completed: Int
)

data class CleaningPermissions(
    val canAssign: Boolean = false,
    val canReassign: Boolean = false,
    val canManage: Boolean = false
)
