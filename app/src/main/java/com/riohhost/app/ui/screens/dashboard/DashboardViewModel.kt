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
                val allReservations = reservationRepository.getReservations()
                
                android.util.Log.d("DashboardVM", "Propriedades: ${properties.size}, Reservas: ${allReservations.size}")
                
                // totalRevenue agora é Double do Supabase
                val totalRevenue = allReservations.sumOf { 
                    it.totalRevenue ?: 0.0 
                }
                
                // Status no Supabase é "Ativo" (português), não "active"
                val activePropertiesCount = properties.count { 
                    it.status?.equals("Ativo", ignoreCase = true) == true ||
                    it.status?.equals("active", ignoreCase = true) == true
                }
                
                android.util.Log.d("DashboardVM", "Total Revenue: $totalRevenue, Active Properties: $activePropertiesCount")

                _uiState.value = DashboardUiState.Success(
                    properties = properties,
                    totalRevenue = totalRevenue,
                    occupancyRate = 0.0,
                    activeProperties = activePropertiesCount
                )
            } catch (e: Exception) {
                android.util.Log.e("DashboardVM", "Erro no dashboard: ${e.message}", e)
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
