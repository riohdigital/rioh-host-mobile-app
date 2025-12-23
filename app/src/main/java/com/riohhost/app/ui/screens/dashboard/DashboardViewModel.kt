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

class DashboardViewModel(
    private val globalFilters: GlobalFiltersViewModel? = null
) : ViewModel() {
    
    private val propertyRepository = PropertyRepository()
    private val reservationRepository = ReservationRepository()
    private val expenseRepository = ExpenseRepository()

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        if (globalFilters != null) {
            observeFilterChanges()
        } else {
            loadDashboardData(
                startDate = DateRangeCalculator.toIsoString(LocalDate.now().withDayOfYear(1)),
                endDate = DateRangeCalculator.toIsoString(LocalDate.now().withMonth(12).withDayOfMonth(31)),
                propertyIds = null,
                platform = null
            )
        }
    }

    private fun observeFilterChanges() {
        viewModelScope.launch {
            combine(
                globalFilters!!.dateRangeStrings,
                globalFilters.selectedProperties,
                globalFilters.selectedPlatform
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

    fun loadDashboardData(
        startDate: String,
        endDate: String,
        propertyIds: List<String>?,
        platform: String?
    ) {
        viewModelScope.launch {
            try {
                _uiState.value = DashboardUiState.Loading
                
                android.util.Log.d("DashboardVM", "Carregando: $startDate a $endDate")
                
                val properties = propertyRepository.getProperties()
                val reservations = reservationRepository.getReservationsFiltered(
                    startDate = startDate,
                    endDate = endDate,
                    propertyIds = propertyIds,
                    platform = platform
                )
                val expenses = expenseRepository.getExpensesFiltered(
                    startDate = startDate,
                    endDate = endDate,
                    propertyIds = propertyIds
                )
                
                android.util.Log.d("DashboardVM", "Dados: ${properties.size} props, ${reservations.size} reservas, ${expenses.size} despesas")
                
                val kpis = calculateKpis(reservations, expenses, properties, startDate, endDate)
                
                _uiState.value = DashboardUiState.Success(
                    kpis = kpis,
                    properties = properties,
                    reservations = reservations
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
        val totalExpenses = expenses.sumOf { it.amount ?: 0.0 }
        val netProfit = netRevenue - totalExpenses
        
        // Active properties
        val activePropertiesCount = properties.count { 
            it.status?.equals("Ativo", ignoreCase = true) == true ||
            it.status?.equals("active", ignoreCase = true) == true
        }
        
        // Occupancy rate
        val occupancyRate = calculateOccupancyRate(
            reservations = reservations,
            startDate = startDate,
            endDate = endDate,
            propertiesCount = max(1, activePropertiesCount)
        )
        
        // Revenue by platform
        val revenueByPlatform = reservations
            .groupBy { it.platform ?: "Direto" }
            .mapValues { (_, list) -> list.sumOf { it.totalRevenue ?: 0.0 } }
        
        // Payment status counts
        val paymentStatusCounts = PaymentStatusCounts(
            paid = reservations.count { it.paymentStatus?.equals("Pago", ignoreCase = true) == true },
            pending = reservations.count { it.paymentStatus?.equals("Pendente", ignoreCase = true) == true },
            overdue = reservations.count { it.paymentStatus?.equals("Atrasado", ignoreCase = true) == true }
        )
        
        return DashboardKpis(
            totalRevenue = totalRevenue,
            netRevenue = netRevenue,
            totalCommission = totalCommission,
            totalExpenses = totalExpenses,
            netProfit = netProfit,
            occupancyRate = occupancyRate,
            activeProperties = activePropertiesCount,
            totalReservations = reservations.size,
            revenueByPlatform = revenueByPlatform,
            paymentStatusCounts = paymentStatusCounts
        )
    }

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
            loadDashboardData(
                startDate = DateRangeCalculator.toIsoString(LocalDate.now().withDayOfYear(1)),
                endDate = DateRangeCalculator.toIsoString(LocalDate.now().withMonth(12).withDayOfMonth(31)),
                propertyIds = null,
                platform = null
            )
        }
    }
}

data class PaymentStatusCounts(
    val paid: Int,
    val pending: Int,
    val overdue: Int
)

data class DashboardKpis(
    val totalRevenue: Double,
    val netRevenue: Double,
    val totalCommission: Double,
    val totalExpenses: Double,
    val netProfit: Double,
    val occupancyRate: Double,
    val activeProperties: Int,
    val totalReservations: Int,
    val revenueByPlatform: Map<String, Double>,
    val paymentStatusCounts: PaymentStatusCounts
)

sealed class DashboardUiState {
    object Loading : DashboardUiState()
    data class Success(
        val kpis: DashboardKpis,
        val properties: List<Property>,
        val reservations: List<Reservation>
    ) : DashboardUiState()
    data class Error(val message: String) : DashboardUiState()
}
