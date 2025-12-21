package com.riohhost.app.ui.screens.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.riohhost.app.data.models.Reservation
import com.riohhost.app.data.repositories.ReservationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

sealed class CalendarUiState {
    object Loading : CalendarUiState()
    data class Success(val reservations: List<Reservation>) : CalendarUiState()
    data class Error(val message: String) : CalendarUiState()
}

class CalendarViewModel(
    private val reservationRepository: ReservationRepository = ReservationRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<CalendarUiState>(CalendarUiState.Loading)
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    init {
        loadReservations()
    }

    fun loadReservations() {
        viewModelScope.launch {
            _uiState.value = CalendarUiState.Loading
            try {
                // Fetch all for now. In real app, filter by month range.
                val reservations = reservationRepository.getReservations()
                _uiState.value = CalendarUiState.Success(reservations)
            } catch (e: Exception) {
                _uiState.value = CalendarUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    // Helper to check efficiently (optimization: map dates to reservations map)
    fun getReservationsForDate(date: LocalDate, reservations: List<Reservation>): List<Reservation> {
        return reservations.filter {
            val checkIn = LocalDate.parse(it.checkInDate, DateTimeFormatter.ISO_DATE)
            val checkOut = LocalDate.parse(it.checkOutDate, DateTimeFormatter.ISO_DATE)
            
            // Check if date is within range [checkIn, checkOut]
            !date.isBefore(checkIn) && !date.isAfter(checkOut)
        }
    }
}
