package com.riohhost.app.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.riohhost.app.data.models.Property
import com.riohhost.app.data.models.Reservation
import com.riohhost.app.data.repositories.ExpenseRepository
import com.riohhost.app.data.repositories.PropertyRepository
import com.riohhost.app.data.repositories.ReservationRepository
import com.riohhost.app.ui.GlobalFiltersViewModel
import com.riohhost.app.utils.DateRangeCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.max

class DashboardViewModel : ViewModel() {
    
    // Injected after creation
    var globalFilters: GlobalFiltersViewModel? = null
        private set
    
    private val propertyRepository = PropertyRepository()
    private val reservationRepository = ReservationRepository()
    private val expenseRepository = ExpenseRepository()

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        // Initial load with defaults
        refresh()
    }
    
    fun setFilters(filters: GlobalFiltersViewModel) {
        if (globalFilters != filters) {
            globalFilters = filters
            observeFilterChanges()
            refresh() // Trigger immediate refresh with new filters
        }
    }

    private fun observeFilterChanges() {
        viewModelScope.launch {
            globalFilters?.let { filters ->
                combine(
                    filters.dateRangeStrings,
                    filters.selectedProperties,
                    filters.selectedPlatform
                ) { dateRange, properties, platform ->
                    Triple(dateRange, properties, platform)
                }.collectLatest { (dateRange, properties, platform) ->
                    loadDashboardData(
                        startDate = dateRange.first,
                        endDate = dateRange.second,
                        propertyIds = if (properties.contains("todas")) null else properties,
                        platform = if (platform == "all") null else platform
                    )
                }
            }
        }
    }

    fun loadDashboardData(
        startDate: String,
        endDate: String,
        propertyIds: List<String>?,
        platform: String?
    ) {
        viewModelScope.launch {
            try {
                _uiState.value = DashboardUiState.Loading
                
                android.util.Log.d("DashboardVM", "Carregando: $startDate a $endDate, props: $propertyIds, platform: $platform")
                
                // Fetch all data in parallel
                var properties: List<Property> = emptyList()
                var reservations: List<Reservation> = emptyList()
                var expenses: List<com.riohhost.app.data.models.Expense> = emptyList()
                var todaysEvents: com.riohhost.app.data.models.TodaysEvents = com.riohhost.app.data.models.TodaysEvents()
                var alerts: List<com.riohhost.app.data.models.OperationalAlert> = emptyList()

                val jobs = listOf(
                    launch { properties = propertyRepository.getProperties() },
                    launch { 
                        reservations = reservationRepository.getReservationsFiltered(
                            startDate = startDate,
                            endDate = endDate,
                            propertyIds = propertyIds,
                            platform = platform
                        )
                    },
                    launch {
                        expenses = expenseRepository.getExpensesFiltered(
                            startDate = startDate,
                            endDate = endDate,
                            propertyIds = propertyIds
                        )
                    },
                    launch { todaysEvents = reservationRepository.getTodaysEvents() },
                    launch { alerts = reservationRepository.getOperationalAlerts() }
                )
                jobs.forEach { it.join() }
                
                android.util.Log.d("DashboardVM", "Dados: ${properties.size} props, ${reservations.size} reservas, ${expenses.size} despesas")
                
                // Calculate KPIs
                val kpis = calculateKpis(
                    reservations = reservations,
                    expenses = expenses,
                    properties = properties,
                    startDate = startDate,
                    endDate = endDate
                )
                
                android.util.Log.d("DashboardVM", "KPIs: $kpis")

                _uiState.value = DashboardUiState.Success(
                    kpis = kpis,
                    properties = properties,
                    reservations = reservations,
                    todaysEvents = todaysEvents,
                    alerts = alerts
                )
            } catch (e: Exception) {
                android.util.Log.e("DashboardVM", "Erro no dashboard: ${e.message}", e)
                _uiState.value = DashboardUiState.Error(e.message ?: "Erro ao carregar dashboard")
            }
        }
    }

    private fun calculateKpis(
        reservations: List<Reservation>,
        expenses: List<com.riohhost.app.data.models.Expense>,
        properties: List<Property>,
        startDate: String,
        endDate: String
    ): DashboardKpis {
        // Revenue calculations
        val totalRevenue = reservations.sumOf { it.totalRevenue ?: 0.0 }
        val netRevenue = reservations.sumOf { it.netRevenue ?: 0.0 }
        val totalCommission = reservations.sumOf { it.commissionAmount ?: 0.0 }
        
        // Expense calculations
        val totalExpenses = expenses.sumOf { it.amount ?: 0.0 }
        
        // Profit
        val netProfit = netRevenue - totalExpenses
        
        // Active properties
        val activePropertiesCount = properties.count { 
            it.status?.equals("Ativo", ignoreCase = true) == true ||
            it.status?.equals("active", ignoreCase = true) == true
        }
        
        // Occupancy rate calculation
        val occupancyRate = calculateOccupancyRate(
            reservations = reservations,
            startDate = startDate,
            endDate = endDate,
            propertiesCount = max(1, activePropertiesCount)
        )
        
        // Revenue by platform
        val revenueByPlatform = reservations
            .groupBy { it.platform ?: "Direto" }
            .mapValues { (_, reservations) -> 
                reservations.sumOf { it.totalRevenue ?: 0.0 } 
            }
        
        // Avg Ticket
        val avgTicket = if (reservations.isNotEmpty()) totalRevenue / reservations.size else 0.0
        
        // Avg Nightly Revenue
        val totalNights = reservations.sumOf { res ->
            val checkIn = res.checkInDate?.let { try { LocalDate.parse(it) } catch(e: Exception) { null } }
            val checkOut = res.checkOutDate?.let { try { LocalDate.parse(it) } catch(e: Exception) { null } }
            
            if (checkIn != null && checkOut != null) {
                ChronoUnit.DAYS.between(checkIn, checkOut).coerceAtLeast(1)
            } else {
                0L
            }
        }
        val avgNightlyRevenue = if (totalNights > 0) totalRevenue / totalNights else 0.0

        return DashboardKpis(
            totalRevenue = totalRevenue,
            netRevenue = netRevenue,
            totalCommission = totalCommission,
            totalExpenses = totalExpenses,
            netProfit = netProfit,
            occupancyRate = occupancyRate,
            activeProperties = activePropertiesCount,
            totalReservations = reservations.size,
            avgTicket = avgTicket,
            avgNightlyRevenue = avgNightlyRevenue,
            revenueByPlatform = revenueByPlatform
        )
    }

    /**
     * Calculates occupancy rate based on days booked within the period.
     * Uses overlap logic: counts only the days of each reservation that fall within the period.
     */
    private fun calculateOccupancyRate(
        reservations: List<Reservation>,
        startDate: String,
        endDate: String,
        propertiesCount: Int
    ): Double {
        val periodStart = DateRangeCalculator.fromIsoString(startDate) ?: return 0.0
        val periodEnd = DateRangeCalculator.fromIsoString(endDate) ?: return 0.0
        
        val totalBookedDays = reservations.sumOf { reservation ->
            val checkIn = reservation.checkInDate?.let { DateRangeCalculator.fromIsoString(it) } ?: return@sumOf 0
            val checkOut = reservation.checkOutDate?.let { DateRangeCalculator.fromIsoString(it) } ?: return@sumOf 0
            
            // Calculate overlap between reservation and period
            val overlapStart = maxOf(checkIn, periodStart)
            val overlapEnd = minOf(checkOut, periodEnd)
            
            if (overlapStart < overlapEnd) {
                ChronoUnit.DAYS.between(overlapStart, overlapEnd).toInt()
            } else {
                0
            }
        }
        
        val totalDays = ChronoUnit.DAYS.between(periodStart, periodEnd).toInt()
        
        return if (totalDays > 0 && propertiesCount > 0) {
            (totalBookedDays.toDouble() / (totalDays * propertiesCount)) * 100
        } else {
            0.0
        }
    }

    /**
     * Refresh data using current filters or defaults.
     */
    fun refresh() {
        if (globalFilters != null) {
            val dateRange = globalFilters.dateRangeStrings.value
            loadDashboardData(
                startDate = dateRange.first,
                endDate = dateRange.second,
                propertyIds = globalFilters.getPropertyFilter(),
                platform = globalFilters.getPlatformFilter()
            )
        } else {
            // Default to current year
            loadDashboardData(
                startDate = DateRangeCalculator.toIsoString(LocalDate.now().withDayOfYear(1)),
                endDate = DateRangeCalculator.toIsoString(LocalDate.now().withMonth(12).withDayOfMonth(31)),
                propertyIds = null,
                platform = null
            )
        }
    }
}

/**
 * All KPIs displayed on the dashboard.
 */
data class DashboardKpis(
    val totalRevenue: Double,
    val netRevenue: Double,
    val totalCommission: Double,
    val totalExpenses: Double,
    val netProfit: Double,
    val occupancyRate: Double,
    val activeProperties: Int,
    val totalReservations: Int,
    val avgTicket: Double,
    val avgNightlyRevenue: Double,
    val revenueByPlatform: Map<String, Double>
)

sealed class DashboardUiState {
    object Loading : DashboardUiState()
    data class Success(
        val kpis: DashboardKpis,
        val properties: List<Property>,
        val reservations: List<Reservation>,
        val todaysEvents: com.riohhost.app.data.models.TodaysEvents = com.riohhost.app.data.models.TodaysEvents(),
        val alerts: List<com.riohhost.app.data.models.OperationalAlert> = emptyList()
    ) : DashboardUiState()
    data class Error(val message: String) : DashboardUiState()
}
