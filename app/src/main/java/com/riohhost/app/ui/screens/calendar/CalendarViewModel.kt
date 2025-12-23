package com.riohhost.app.ui.screens.calendar

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

class CalendarViewModel : ViewModel() {
    private val repository = ReservationRepository()

    private val _uiState = MutableStateFlow<CalendarUiState>(CalendarUiState.Loading)
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    init {
        // Load with general period to show all reservations
        val today = LocalDate.now()
        loadReservationsFiltered(
            startDate = DateRangeCalculator.toIsoString(today.minusMonths(3)),
            endDate = DateRangeCalculator.toIsoString(today.plusMonths(12)),
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
                _uiState.value = CalendarUiState.Loading
                android.util.Log.d("CalendarVM", "Loading: $startDate to $endDate")
                
                val reservations = repository.getReservationsFiltered(
                    startDate = startDate,
                    endDate = endDate,
                    propertyIds = propertyIds,
                    platform = platform
                )
                
                android.util.Log.d("CalendarVM", "Loaded ${reservations.size} reservations")
                _uiState.value = CalendarUiState.Success(reservations)
            } catch (e: Exception) {
                android.util.Log.e("CalendarVM", "Error: ${e.message}", e)
                _uiState.value = CalendarUiState.Error(e.message ?: "Erro ao carregar")
            }
        }
    }

    fun getReservationsForDate(date: LocalDate, reservations: List<Reservation>): List<Reservation> {
        return reservations.filter { reservation ->
            val checkIn = reservation.checkInDate?.let { LocalDate.parse(it) }
            val checkOut = reservation.checkOutDate?.let { LocalDate.parse(it) }
            if (checkIn != null && checkOut != null) {
                !date.isBefore(checkIn) && !date.isAfter(checkOut)
            } else {
                false
            }
        }
    }
}

sealed class CalendarUiState {
    object Loading : CalendarUiState()
    data class Success(val reservations: List<Reservation>) : CalendarUiState()
    data class Error(val message: String) : CalendarUiState()
}
