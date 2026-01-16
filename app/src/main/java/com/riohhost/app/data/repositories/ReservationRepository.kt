package com.riohhost.app.data.repositories

import com.riohhost.app.data.api.SupabaseClient
import com.riohhost.app.data.models.Reservation
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import io.github.jan.supabase.postgrest.query.filter.FilterOperator

class ReservationRepository {
    private val supabase = SupabaseClient.client

    suspend fun getReservations(propertyId: String? = null): List<Reservation> {
        return try {
            println("ReservationRepo: Iniciando busca de reservas...")
            val result = supabase.postgrest.from("reservations")
                .select {
                    if (propertyId != null) {
                        filter { eq("property_id", propertyId) }
                    }
                }
                .decodeList<Reservation>()
            println("ReservationRepo: Encontradas ${result.size} reservas")
            if (result.isNotEmpty()) {
                println("ReservationRepo: Primeira reserva: ${result.first()}")
            }
            result
        } catch (e: Exception) {
            println("ReservationRepo: ERRO ao buscar reservas: ${e.message}")
            emptyList()
        }
    }

    /**
     * Get reservations filtered by date range, properties, and platform.
     * Uses overlap logic: check_out >= startDate AND check_in <= endDate
     * This captures all reservations that overlap with the period.
     */
    suspend fun getReservationsFiltered(
        startDate: String,
        endDate: String,
        propertyIds: List<String>? = null,
        platform: String? = null
    ): List<Reservation> {
        return try {
            android.util.Log.d("ReservationRepo", "Buscando reservas: $startDate a $endDate, props: $propertyIds, platform: $platform")
            
            val result = supabase.postgrest.from("reservations").select {
                filter {
                    gte("check_out_date", startDate)
                    lte("check_in_date", endDate)
                    if (!platform.isNullOrEmpty() && platform != "all") {
                        eq("platform", platform)
                    }
                }
            }.decodeList<Reservation>()
            
            // Property filter (applied client-side due to API limitations)
            val filteredResult = if (!propertyIds.isNullOrEmpty() && !propertyIds.contains("todas")) {
                result.filter { reservation -> 
                    reservation.propertyId?.let { propertyIds.contains(it) } ?: false
                }
            } else {
                result
            }
            
            android.util.Log.d("ReservationRepo", "Encontradas ${filteredResult.size} reservas filtradas")
            filteredResult
        } catch (e: Exception) {
            android.util.Log.e("ReservationRepo", "ERRO ao buscar reservas filtradas: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun getReservationById(id: String): Reservation? {
        return try {
            supabase.postgrest.from("reservations")
                .select {
                    filter { eq("id", id) }
                }
                .decodeSingle<Reservation>()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getCleanerReservations(cleanerId: String): List<Reservation> {
        return try {
            supabase.postgrest.rpc("fn_get_cleaner_reservations", mapOf("cleaner_id" to cleanerId))
                .decodeList<Reservation>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getAvailableReservations(cleanerId: String): List<Reservation> {
        return try {
            supabase.postgrest.rpc("fn_get_available_reservations", mapOf("cleaner_id" to cleanerId))
                .decodeList<Reservation>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun assignCleaning(reservationId: String, cleanerId: String) {
        try {
            supabase.postgrest.rpc("assign_cleaning_with_permissions", 
                mapOf("reservation_id" to reservationId, "cleaner_id" to cleanerId))
        } catch (e: Exception) {
            // Handle error or rethrow
        }
    }

    suspend fun toggleCleaningStatus(reservationId: String) {
        try {
            supabase.postgrest.rpc("fn_toggle_cleaning_status", mapOf("reservation_id" to reservationId))
        } catch (e: Exception) {
            // Handle error or rethrow
        }
    }

    /**
     * Busca reservas aplicando regras específicas de data por plataforma para fins FINANCEIROS.
     * - Airbnb/Direto: Filtra por check_in_date
     * - Booking.com: Filtra por check_out_date
     */
    suspend fun fetchReservationsWithPlatformRules(
        startDate: String,
        endDate: String,
        propertyIds: List<String>? = null,
        platform: String? = null
    ): List<Reservation> {
        return try {
            android.util.Log.d("ReservationRepo", "Buscando reservas (Financeiro): $startDate a $endDate, platform: $platform")
            val allReservations = mutableListOf<Reservation>()

            if (platform == null || platform == "all") {
                // 1. Booking.com (Check-out)
                val bookingQuery = supabase.postgrest.from("reservations").select {
                    filter {
                        gte("check_out_date", startDate)
                        lte("check_out_date", endDate)
                        isIn("reservation_status", listOf("Confirmada", "Em Andamento", "Finalizada"))
                        eq("platform", "Booking.com")
                        
                        if (!propertyIds.isNullOrEmpty() && !propertyIds.contains("todas")) {
                            isIn("property_id", propertyIds)
                        }
                    }
                }.decodeList<Reservation>()
                allReservations.addAll(bookingQuery)

                // 2. Others (Non-Booking) -> Check-in
                // 2a. Platforms != Booking.com
                val nonBookingQuery = supabase.postgrest.from("reservations").select {
                    filter {
                        gte("check_in_date", startDate)
                        lte("check_in_date", endDate)
                        isIn("reservation_status", listOf("Confirmada", "Em Andamento", "Finalizada"))
                        neq("platform", "Booking.com")
                        
                        if (!propertyIds.isNullOrEmpty() && !propertyIds.contains("todas")) {
                            isIn("property_id", propertyIds)
                        }
                    }
                }.decodeList<Reservation>()
                allReservations.addAll(nonBookingQuery)

                // 2b. Platform IS NULL (defaults to standard/check-in rules)
                val nullPlatformQuery = supabase.postgrest.from("reservations").select {
                    filter {
                        gte("check_in_date", startDate)
                        lte("check_in_date", endDate)
                        isIn("reservation_status", listOf("Confirmada", "Em Andamento", "Finalizada"))
                        filter("platform", FilterOperator.IS, null)
                        
                        if (!propertyIds.isNullOrEmpty() && !propertyIds.contains("todas")) {
                            isIn("property_id", propertyIds)
                        }
                    }
                }.decodeList<Reservation>()
                allReservations.addAll(nullPlatformQuery)

            } else if (platform == "Booking.com") {
                 val bookingQuery = supabase.postgrest.from("reservations").select {
                    filter {
                        gte("check_out_date", startDate)
                        lte("check_out_date", endDate)
                        isIn("reservation_status", listOf("Confirmada", "Em Andamento", "Finalizada"))
                        eq("platform", "Booking.com")
                        
                        if (!propertyIds.isNullOrEmpty() && !propertyIds.contains("todas")) {
                           isIn("property_id", propertyIds)
                        }
                    }
                }.decodeList<Reservation>()
                allReservations.addAll(bookingQuery)
            } else {
                // Specific platform (Airbnb, Direto, etc) -> Check-in
                val query = supabase.postgrest.from("reservations").select {
                    filter {
                        gte("check_in_date", startDate)
                        lte("check_in_date", endDate)
                        isIn("reservation_status", listOf("Confirmada", "Em Andamento", "Finalizada"))
                        eq("platform", platform)
                        
                        if (!propertyIds.isNullOrEmpty() && !propertyIds.contains("todas")) {
                            isIn("property_id", propertyIds)
                        }
                    }
                }.decodeList<Reservation>()
                allReservations.addAll(query)
            }
            
            android.util.Log.d("ReservationRepo", "Total Financeiro encontrado: ${allReservations.size}")
            allReservations
        } catch (e: Exception) {
            android.util.Log.e("ReservationRepo", "Erro ao buscar reservas financeiras: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun getCleanersForProperty(propertyId: String): Result<List<com.riohhost.app.data.models.CleanerInfo>> {
        return try {
            val response = supabase.postgrest
                .rpc("fn_get_property_cleaners_for_user", mapOf("p_property_id" to propertyId))
                .decodeList<com.riohhost.app.data.models.CleanerInfo>()
            Result.success(response)
        } catch (e: Exception) {
            println("ReservationRepo: Erro ao buscar faxineiras: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun createReservation(data: com.riohhost.app.data.models.ReservationFormData): Result<Reservation> {
        val (cleanerId, cleaningAllocation) = processCleaningDestination(data.cleaning_destination)
        
        val submissionData = mapOf(
            "property_id" to data.property_id,
            "platform" to data.platform,
            "reservation_code" to data.reservation_code,
            "guest_name" to data.guest_name,
            "guest_email" to data.guest_email,
            "guest_phone" to data.guest_phone,
            "number_of_guests" to data.number_of_guests,
            "check_in_date" to data.check_in_date,
            "check_out_date" to data.check_out_date,
            "checkin_time" to data.checkin_time,
            "checkout_time" to data.checkout_time,
            "total_revenue" to data.total_revenue.toBigDecimalOrNull(),
            "payment_status" to data.payment_status,
            "reservation_status" to data.reservation_status,
            "cleaner_user_id" to cleanerId,
            "cleaning_allocation" to cleaningAllocation,
            "cleaning_payment_status" to data.cleaning_payment_status,
            "cleaning_rating" to data.cleaning_rating,
            "cleaning_notes" to data.cleaning_notes,
            "cleaning_fee" to data.cleaning_fee?.toBigDecimalOrNull()
        )
        
        return try {
            val result = supabase.postgrest.from("reservations")
                .insert(submissionData)
                .decodeSingle<Reservation>()
            Result.success(result)
        } catch (e: Exception) {
            println("ReservationRepo: Erro ao criar reserva: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun updateReservation(id: String, data: com.riohhost.app.data.models.ReservationFormData): Result<Reservation> {
        val (cleanerId, cleaningAllocation) = processCleaningDestination(data.cleaning_destination)

        val submissionData = mapOf(
            "property_id" to data.property_id,
            "platform" to data.platform,
            "reservation_code" to data.reservation_code,
            "guest_name" to data.guest_name,
            "guest_email" to data.guest_email,
            "guest_phone" to data.guest_phone,
            "number_of_guests" to data.number_of_guests,
            "check_in_date" to data.check_in_date,
            "check_out_date" to data.check_out_date,
            "checkin_time" to data.checkin_time,
            "checkout_time" to data.checkout_time,
            "total_revenue" to data.total_revenue.toBigDecimalOrNull(),
            "payment_status" to data.payment_status,
            "reservation_status" to data.reservation_status,
            "cleaner_user_id" to cleanerId,
            "cleaning_allocation" to cleaningAllocation,
            "cleaning_payment_status" to data.cleaning_payment_status,
            "cleaning_rating" to data.cleaning_rating,
            "cleaning_notes" to data.cleaning_notes,
            "cleaning_fee" to data.cleaning_fee?.toBigDecimalOrNull()
        )

        return try {
            val result = supabase.postgrest.from("reservations")
                .update(submissionData) {
                    filter {
                        eq("id", id)
                    }
                }
                .decodeSingle<Reservation>()
            Result.success(result)
        } catch (e: Exception) {
            println("ReservationRepo: Erro ao atualizar reserva: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun getNextReservations(propertyId: String, limit: Int = 5): List<Reservation> {
        return try {
            val today = java.time.LocalDate.now().toString()
            supabase.postgrest.from("reservations")
                .select {
                    filter {
                        eq("property_id", propertyId)
                        gte("check_in_date", today)
                        eq("reservation_status", "Confirmada")
                    }
                    order("check_in_date", io.github.jan.supabase.postgrest.query.Order.ASCENDING)
                    limit(limit.toLong())
                }
                .decodeList<Reservation>()
        } catch (e: Exception) {
            println("ReservationRepo: Erro ao buscar proximas reservas: ${e.message}")
            emptyList()
        }
    }

    suspend fun getReservationsForStats(propertyId: String, startDate: String, endDate: String): List<Reservation> {
        return try {
            supabase.postgrest.from("reservations")
                .select {
                    filter {
                        eq("property_id", propertyId)
                        gte("check_in_date", startDate)
                        lte("check_out_date", endDate)
                        isIn("reservation_status", listOf("Confirmada", "Em Andamento", "Finalizada"))
                    }
                }
                .decodeList<Reservation>()
        } catch (e: Exception) {
             println("ReservationRepo: Erro ao buscar reservas para stats: ${e.message}")
            emptyList()
        }
    }

    suspend fun getTodaysEvents(): com.riohhost.app.data.models.TodaysEvents {
        return try {
            val today = java.time.LocalDate.now().toString()
            
            // Check-ins today
            val checkins = supabase.postgrest.from("reservations").select {
                count(io.github.jan.supabase.postgrest.query.Count.EXACT)
                filter {
                    eq("check_in_date", today)
                    eq("reservation_status", "Confirmada")
                }
            }.countOrNull() ?: 0

            // Check-outs today
            val checkouts = supabase.postgrest.from("reservations").select {
                count(io.github.jan.supabase.postgrest.query.Count.EXACT)
                filter {
                    eq("check_out_date", today)
                    isIn("reservation_status", listOf("Confirmada", "Em Andamento"))
                }
            }.countOrNull() ?: 0

            // Cleanings today (based on checkout)
            val cleanings = supabase.postgrest.from("reservations").select {
                count(io.github.jan.supabase.postgrest.query.Count.EXACT)
                filter {
                    eq("check_out_date", today)
                    neq("cleaning_status", "Realizada")
                    isIn("reservation_status", listOf("Confirmada", "Em Andamento", "Finalizada"))
                }
            }.countOrNull() ?: 0

            com.riohhost.app.data.models.TodaysEvents(checkins.toInt(), checkouts.toInt(), cleanings.toInt())
        } catch (e: Exception) {
            println("ReservationRepo: Erro ao buscar eventos de hoje: ${e.message}")
            com.riohhost.app.data.models.TodaysEvents()
        }
    }

    suspend fun getOperationalAlerts(): List<com.riohhost.app.data.models.OperationalAlert> {
        val alerts = mutableListOf<com.riohhost.app.data.models.OperationalAlert>()
        try {
            val today = java.time.LocalDate.now()
            
            // 1. Reserves without cleaner (Checkout in next 3 days)
            val threeDaysFromNow = today.plusDays(3).toString()
            val noCleanerCount = supabase.postgrest.from("reservations").select {
                count(io.github.jan.supabase.postgrest.query.Count.EXACT)
                filter {
                    gte("check_out_date", today.toString())
                    lte("check_out_date", threeDaysFromNow)
                    isIn("reservation_status", listOf("Confirmada", "Em Andamento"))
                    // Using filter for null check
                    filter("cleaner_user_id", FilterOperator.IS, null)
                }
            }.countOrNull() ?: 0

            if (noCleanerCount > 0) {
                alerts.add(com.riohhost.app.data.models.OperationalAlert(
                    type = "no_cleaner",
                    count = noCleanerCount.toInt(),
                    message = "$noCleanerCount reservas sem faxineira atribuída",
                    severity = "critical"
                ))
            }

            // 2. Guests not communicated (Check-in in 24h)
            val tomorrow = today.plusDays(1).toString()
            val notCommunicatedCount = supabase.postgrest.from("reservations").select {
                count(io.github.jan.supabase.postgrest.query.Count.EXACT)
                filter {
                    gte("check_in_date", today.toString())
                    lte("check_in_date", tomorrow)
                    eq("reservation_status", "Confirmada")
                    eq("is_communicated", false)
                }
            }.countOrNull() ?: 0

            if (notCommunicatedCount > 0) {
                alerts.add(com.riohhost.app.data.models.OperationalAlert(
                    type = "no_communication",
                    count = notCommunicatedCount.toInt(),
                    message = "$notCommunicatedCount hóspedes não comunicados (check-in próximo)",
                    severity = "warning"
                ))
            }

            // 3. Pending Payments (Total)
            val pendingPayments = supabase.postgrest.from("reservations").select {
                filter {
                    eq("payment_status", "Pendente")
                    isIn("reservation_status", listOf("Confirmada", "Finalizada"))
                }
            }.decodeList<Reservation>()
            
            val totalPendingValue = pendingPayments.sumOf { it.totalRevenue ?: 0.0 }
            
            if (pendingPayments.isNotEmpty()) {
                val formattedValue = com.riohhost.app.utils.CurrencyUtils.formatBRL(totalPendingValue)
                alerts.add(com.riohhost.app.data.models.OperationalAlert(
                    type = "pending_payment",
                    count = pendingPayments.size,
                    message = "$formattedValue em pagamentos pendentes",
                    severity = "warning"
                ))
            }

        } catch (e: Exception) {
            println("ReservationRepo: Erro ao buscar alertas: ${e.message}")
        }
        return alerts
    }

    private fun processCleaningDestination(destination: String?): Pair<String?, String?> {
        val uuidRegex = "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$".toRegex(RegexOption.IGNORE_CASE)
        
        return when {
            destination?.matches(uuidRegex) == true -> {
                // É um UUID de faxineira
                Pair(destination, null)  // cleaner_user_id, cleaning_allocation
            }
            destination == "host" -> {
                Pair(null, "co_anfitriao")
            }
            destination == "owner" -> {
                Pair(null, "proprietario")
            }
            else -> {
                Pair(null, null)  // Nenhum
            }
        }
    }
}
