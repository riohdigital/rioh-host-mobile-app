package com.riohhost.app.ui.screens.reservations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.riohhost.app.data.models.Reservation
import com.riohhost.app.data.repositories.ReservationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ReservationViewModel : ViewModel() {
    private val reservationRepository = ReservationRepository()

    private val _uiState = MutableStateFlow<ReservationsUiState>(ReservationsUiState.Loading)
    val uiState: StateFlow<ReservationsUiState> = _uiState.asStateFlow()

    init {
        loadReservations()
    }

    private fun loadReservations() {
        viewModelScope.launch {
            _uiState.value = ReservationsUiState.Loading
            try {
                val reservations = reservationRepository.getReservations()
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
