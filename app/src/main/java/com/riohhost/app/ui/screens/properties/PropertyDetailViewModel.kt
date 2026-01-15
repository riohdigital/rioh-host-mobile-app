package com.riohhost.app.ui.screens.properties

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.riohhost.app.data.models.Property
import com.riohhost.app.data.models.PropertyCleaner
import com.riohhost.app.data.models.Reservation
import com.riohhost.app.data.repositories.PropertyRepository
import com.riohhost.app.data.repositories.ReservationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class PropertyDetailUiState(
    val property: Property? = null,
    val isLoading: Boolean = false,
    val nextReservations: List<Reservation> = emptyList(),
    val cleaners: List<PropertyCleaner> = emptyList(),
    val occupancyRate: Double = 0.0,
    val totalRevenue: Double = 0.0,
    val reservationCount: Int = 0,
    val avgTicket: Double = 0.0,
    val error: String? = null
)

class PropertyDetailViewModel(
    private val propertyRepository: PropertyRepository = PropertyRepository(),
    private val reservationRepository: ReservationRepository = ReservationRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(PropertyDetailUiState())
    val uiState = _uiState.asStateFlow()
    
    // Backward compatibility for property flow if needed, but better to migrate UI
    @Deprecated("Use uiState.property instead")
    val property = _uiState.map { it.property }

    fun loadProperty(id: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            // 1. Load Property Basic Info
            val property = propertyRepository.getPropertyById(id)
            
            if (property == null) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Propriedade não encontrada")
                return@launch
            }

            // 2. Load Parallel Data
            val today = LocalDate.now()
            val startOfMonth = today.withDayOfMonth(1)
            val endOfMonth = today.withDayOfMonth(today.lengthOfMonth())

            var nextReservations: List<Reservation> = emptyList()
            var cleaners: List<PropertyCleaner> = emptyList()
            var kpiReservations: List<Reservation> = emptyList()

            val jobs = listOf(
                launch {
                    nextReservations = reservationRepository.getNextReservations(id, limit = 5)
                },
                launch {
                    cleaners = propertyRepository.getPropertyCleaners(id)
                },
                launch {
                    kpiReservations = reservationRepository.getReservationsForStats(
                        id, 
                        startOfMonth.toString(), 
                        endOfMonth.toString()
                    )
                }
            )
            jobs.forEach { it.join() }

            // 3. Calculate KPIs
            val (occupancy, revenue, count, ticket) = calculateKPIs(kpiReservations, startOfMonth, endOfMonth)

            _uiState.value = PropertyDetailUiState(
                property = property,
                isLoading = false,
                nextReservations = nextReservations,
                cleaners = cleaners,
                occupancyRate = occupancy,
                totalRevenue = revenue,
                reservationCount = count,
                avgTicket = ticket
            )
        }
    }

    private fun calculateKPIs(reservations: List<Reservation>, startDate: LocalDate, endDate: LocalDate): CreateKpiResult {
        if (reservations.isEmpty()) return CreateKpiResult(0.0, 0.0, 0, 0.0)

        var totalNights = 0L
        var totalRev = 0.0

        reservations.forEach { res ->
            // Revenue
            totalRev += (res.totalRevenue ?: 0.0)

            // Nights (Approximation based on user request: sum(check_out - check_in))
            // We should be careful about dates outside the range if using overlap, but the query was strict inclusive or overlap?
            // The query I implemented: check_in >= start AND check_out <= end. STRICT INCLUSION.
            // So we can just sum the nights of these reservations.
            val checkIn = LocalDate.parse(res.checkInDate)
            val checkOut = LocalDate.parse(res.checkOutDate)
            val nights = ChronoUnit.DAYS.between(checkIn, checkOut)
            totalNights += nights
        }

        val daysInPeriod = ChronoUnit.DAYS.between(startDate, endDate) + 1 // inclusive
        val occupancy = if (daysInPeriod > 0) (totalNights.toDouble() / daysInPeriod) * 100 else 0.0
        val count = reservations.size
        val ticket = if (count > 0) totalRev / count else 0.0

        return CreateKpiResult(occupancy, totalRev, count, ticket)
    }

    data class CreateKpiResult(
        val occupancy: Double,
        val revenue: Double,
        val count: Int,
        val ticket: Double
    )
}
