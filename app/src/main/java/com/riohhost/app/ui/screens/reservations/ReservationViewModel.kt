package com.riohhost.app.ui.screens.reservations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.riohhost.app.data.models.Reservation
import com.riohhost.app.data.repositories.ReservationRepository
import com.riohhost.app.utils.DateRangeCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

class ReservationViewModel : ViewModel() {
    private val repository = ReservationRepository()

    private val _uiState = MutableStateFlow<ReservationsUiState>(ReservationsUiState.Loading)
    val uiState: StateFlow<ReservationsUiState> = _uiState.asStateFlow()

    init {
        // Load with current year as default
        val today = LocalDate.now()
        loadReservationsFiltered(
            startDate = DateRangeCalculator.toIsoString(today.withDayOfYear(1)),
            endDate = DateRangeCalculator.toIsoString(today.withMonth(12).withDayOfMonth(31)),
            propertyIds = null,
            platform = null
        )
    }

    fun loadReservationsFiltered(
        startDate: String,
        endDate: String,
        propertyIds: List<String>?,
        platform: String?
    ) {
        viewModelScope.launch {
            try {
                _uiState.value = ReservationsUiState.Loading
                android.util.Log.d("ReservationVM", "Loading: $startDate to $endDate, platform: $platform")
                
                val reservations = repository.getReservationsFiltered(
                    startDate = startDate,
                    endDate = endDate,
                    propertyIds = propertyIds,
                    platform = platform
                )
                
                android.util.Log.d("ReservationVM", "Loaded ${reservations.size} reservations")
                _uiState.value = ReservationsUiState.Success(reservations)
            } catch (e: Exception) {
                android.util.Log.e("ReservationVM", "Error: ${e.message}", e)
                _uiState.value = ReservationsUiState.Error(e.message ?: "Erro ao carregar reservas")
            }
        }
    }

    fun loadReservations() {
        viewModelScope.launch {
            try {
                _uiState.value = ReservationsUiState.Loading
                val reservations = repository.getReservations()
                _uiState.value = ReservationsUiState.Success(reservations)
            } catch (e: Exception) {
                _uiState.value = ReservationsUiState.Error(e.message ?: "Erro ao carregar reservas")
            }
        }
    }
}

sealed class ReservationsUiState {
    object Loading : ReservationsUiState()
    data class Success(val reservations: List<Reservation>) : ReservationsUiState()
    data class Error(val message: String) : ReservationsUiState()
}
