package com.riohhost.app.ui.screens.cleaner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.riohhost.app.data.models.Reservation
import com.riohhost.app.data.repositories.AuthRepository
import com.riohhost.app.data.repositories.ReservationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CleanerViewModel : ViewModel() {
    private val authRepository = AuthRepository()
    private val reservationRepository = ReservationRepository()

    private val _uiState = MutableStateFlow<CleanerUiState>(CleanerUiState.Loading)
    val uiState: StateFlow<CleanerUiState> = _uiState.asStateFlow()

    init {
        loadCleanerData()
    }

    fun loadCleanerData() {
        viewModelScope.launch {
            _uiState.value = CleanerUiState.Loading
            try {
                val user = authRepository.getCurrentUser()
                if (user != null) {
                    val assigned = reservationRepository.getCleanerReservations(user.userId)
                    val available = reservationRepository.getAvailableReservations(user.userId)
                    _uiState.value = CleanerUiState.Success(
                        assignedReservations = assigned,
                        availableReservations = available
                    )
                } else {
                    _uiState.value = CleanerUiState.Error("Usuário não identificado")
                }
            } catch (e: Exception) {
                _uiState.value = CleanerUiState.Error(e.message ?: "Erro ao carregar dados")
            }
        }
    }

    fun acceptCleaning(reservationId: String) {
        viewModelScope.launch {
            val user = authRepository.getCurrentUser() ?: return@launch
            try {
                reservationRepository.assignCleaning(reservationId, user.userId)
                loadCleanerData() // Refresh
            } catch (e: Exception) {
                // Show error
            }
        }
    }

    fun toggleStatus(reservationId: String) {
        viewModelScope.launch {
            try {
                reservationRepository.toggleCleaningStatus(reservationId)
                loadCleanerData() // Refresh
            } catch (e: Exception) {
                // Show error
            }
        }
    }
}

sealed class CleanerUiState {
    object Loading : CleanerUiState()
    data class Success(
        val assignedReservations: List<Reservation>,
        val availableReservations: List<Reservation>
    ) : CleanerUiState()
    data class Error(val message: String) : CleanerUiState()
}
