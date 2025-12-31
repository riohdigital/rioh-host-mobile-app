package com.riohhost.app.ui.screens.reservations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.riohhost.app.data.models.CleanerInfo
import com.riohhost.app.data.models.Reservation
import com.riohhost.app.data.models.ReservationFormData
import com.riohhost.app.data.repositories.ReservationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class FormUiState {
    object Idle : FormUiState()
    object Loading : FormUiState()
    object Success : FormUiState()
    data class Error(val message: String) : FormUiState()
}

class ReservationFormViewModel : ViewModel() {
    private val repository = ReservationRepository()

    private val _uiState = MutableStateFlow<FormUiState>(FormUiState.Idle)
    val uiState: StateFlow<FormUiState> = _uiState.asStateFlow()
    
    // Loaded data for Editing
    private val _existingReservation = MutableStateFlow<Reservation?>(null)
    val existingReservation: StateFlow<Reservation?> = _existingReservation.asStateFlow()

    private val _cleaners = MutableStateFlow<List<CleanerInfo>>(emptyList())
    val cleaners: StateFlow<List<CleanerInfo>> = _cleaners.asStateFlow()

    fun loadReservation(id: String) {
        viewModelScope.launch {
            _uiState.value = FormUiState.Loading
            val result = repository.getReservationById(id)
            if (result != null) {
                _existingReservation.value = result
                // If property is set, load cleaners
                result.propertyId?.let { loadCleaners(it) }
                _uiState.value = FormUiState.Idle
            } else {
                _uiState.value = FormUiState.Error("Reserva não encontrada")
            }
        }
    }

    fun loadCleaners(propertyId: String) {
        viewModelScope.launch {
            val result = repository.getCleanersForProperty(propertyId)
            result.onSuccess { 
                _cleaners.value = it 
            }.onFailure {
                // Ignore error, just empty list
                _cleaners.value = emptyList()
            }
        }
    }

    fun submitForm(
        isEdit: Boolean, 
        reservationId: String?, 
        formData: ReservationFormData
    ) {
        viewModelScope.launch {
            _uiState.value = FormUiState.Loading
            val result = if (isEdit && reservationId != null) {
                repository.updateReservation(reservationId, formData)
            } else {
                repository.createReservation(formData)
            }

            result.onSuccess {
                _uiState.value = FormUiState.Success
            }.onFailure { e ->
                _uiState.value = FormUiState.Error(e.message ?: "Erro ao salvar reserva")
            }
        }
    }
}
