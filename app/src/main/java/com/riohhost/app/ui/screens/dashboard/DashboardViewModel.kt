package com.riohhost.app.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.riohhost.app.data.models.Property
import com.riohhost.app.data.repositories.PropertyRepository
import com.riohhost.app.data.repositories.ReservationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DashboardViewModel : ViewModel() {
    private val propertyRepository = PropertyRepository()
    private val reservationRepository = ReservationRepository()

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            try {
                _uiState.value = DashboardUiState.Loading
                val properties = propertyRepository.getProperties()
                // In a real app, we would aggregate data from reservations here to calculate metrics
                // For now, we fetch reservations to demonstrate connection
                val allReservations = reservationRepository.getReservations()
                
                val totalRevenue = allReservations.sumOf { it.totalRevenue ?: 0.0 }
                val activePropertiesCount = properties.count { it.status == "active" } // Assumption on status

                _uiState.value = DashboardUiState.Success(
                    properties = properties,
                    totalRevenue = totalRevenue,
                    occupancyRate = 0.0, // Placeholder calculation
                    activeProperties = activePropertiesCount
                )
            } catch (e: Exception) {
                _uiState.value = DashboardUiState.Error(e.message ?: "Erro ao carregar dashboard")
            }
        }
    }
}

sealed class DashboardUiState {
    object Loading : DashboardUiState()
    data class Success(
        val properties: List<Property>,
        val totalRevenue: Double,
        val occupancyRate: Double,
        val activeProperties: Int
    ) : DashboardUiState()
    data class Error(val message: String) : DashboardUiState()
}
