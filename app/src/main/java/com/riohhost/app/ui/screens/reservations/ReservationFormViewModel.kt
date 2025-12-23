package com.riohhost.app.ui.screens.reservations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.riohhost.app.data.models.Property
import com.riohhost.app.data.models.ReservationCreate
import com.riohhost.app.data.models.ReservationUpdate
import com.riohhost.app.data.repositories.PropertyRepository
import com.riohhost.app.data.repositories.ReservationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ReservationFormViewModel : ViewModel() {
    private val reservationRepository = ReservationRepository()
    private val propertyRepository = PropertyRepository()

    private val _uiState = MutableStateFlow<ReservationFormUiState>(ReservationFormUiState.Idle)
    val uiState: StateFlow<ReservationFormUiState> = _uiState.asStateFlow()

    private val _formState = MutableStateFlow(ReservationFormState())
    val formState: StateFlow<ReservationFormState> = _formState.asStateFlow()

    private val _properties = MutableStateFlow<List<Property>>(emptyList())
    val properties: StateFlow<List<Property>> = _properties.asStateFlow()

    fun loadProperties() {
        viewModelScope.launch {
            _properties.value = propertyRepository.getProperties()
        }
    }

    fun loadReservation(id: String) {
        viewModelScope.launch {
            _uiState.value = ReservationFormUiState.Loading
            val reservation = reservationRepository.getReservationById(id)
            if (reservation != null) {
                _formState.value = ReservationFormState(
                    propertyId = reservation.propertyId ?: "",
                    platform = reservation.platform ?: "Airbnb",
                    reservationCode = reservation.reservationCode ?: "",
                    checkInDate = reservation.checkInDate ?: "",
                    checkOutDate = reservation.checkOutDate ?: "",
                    guestName = reservation.guestName ?: "",
                    guestPhone = reservation.guestPhone ?: "",
                    guestEmail = reservation.guestEmail ?: "",
                    numberOfGuests = reservation.numberOfGuests?.toString() ?: "",
                    totalRevenue = reservation.totalRevenue?.toString() ?: "",
                    reservationStatus = reservation.reservationStatus ?: "Confirmada",
                    paymentStatus = reservation.paymentStatus ?: "Pendente"
                )
                _uiState.value = ReservationFormUiState.Idle
            } else {
                _uiState.value = ReservationFormUiState.Error("Reserva não encontrada")
            }
        }
    }

    fun createReservation() {
        val form = _formState.value
        if (!form.isValid) {
            _uiState.value = ReservationFormUiState.Error("Preencha todos os campos obrigatórios")
            return
        }

        viewModelScope.launch {
            _uiState.value = ReservationFormUiState.Saving
            val reservation = ReservationCreate(
                propertyId = form.propertyId,
                platform = form.platform,
                reservationCode = form.reservationCode,
                checkInDate = form.checkInDate,
                checkOutDate = form.checkOutDate,
                guestName = form.guestName.ifBlank { null },
                guestPhone = form.guestPhone.ifBlank { null },
                guestEmail = form.guestEmail.ifBlank { null },
                numberOfGuests = form.numberOfGuests.toIntOrNull(),
                totalRevenue = form.totalRevenue.toDoubleOrNull() ?: 0.0,
                reservationStatus = form.reservationStatus,
                paymentStatus = form.paymentStatus.ifBlank { null }
            )

            val result = reservationRepository.createReservation(reservation)
            if (result.isSuccess) {
                _uiState.value = ReservationFormUiState.Success
            } else {
                _uiState.value = ReservationFormUiState.Error(result.exceptionOrNull()?.message ?: "Erro ao criar reserva")
            }
        }
    }

    fun updateReservation(id: String) {
        val form = _formState.value
        viewModelScope.launch {
            _uiState.value = ReservationFormUiState.Saving
            val updates = ReservationUpdate(
                propertyId = form.propertyId,
                platform = form.platform,
                reservationCode = form.reservationCode,
                checkInDate = form.checkInDate,
                checkOutDate = form.checkOutDate,
                guestName = form.guestName.ifBlank { null },
                guestPhone = form.guestPhone.ifBlank { null },
                guestEmail = form.guestEmail.ifBlank { null },
                numberOfGuests = form.numberOfGuests.toIntOrNull(),
                totalRevenue = form.totalRevenue.toDoubleOrNull(),
                reservationStatus = form.reservationStatus,
                paymentStatus = form.paymentStatus.ifBlank { null }
            )

            val result = reservationRepository.updateReservation(id, updates)
            if (result.isSuccess) {
                _uiState.value = ReservationFormUiState.Success
            } else {
                _uiState.value = ReservationFormUiState.Error(result.exceptionOrNull()?.message ?: "Erro ao atualizar reserva")
            }
        }
    }

    // Form field updaters
    fun updatePropertyId(value: String) { _formState.value = _formState.value.copy(propertyId = value) }
    fun updatePlatform(value: String) { _formState.value = _formState.value.copy(platform = value) }
    fun updateReservationCode(value: String) { _formState.value = _formState.value.copy(reservationCode = value) }
    fun updateCheckInDate(value: String) { _formState.value = _formState.value.copy(checkInDate = value) }
    fun updateCheckOutDate(value: String) { _formState.value = _formState.value.copy(checkOutDate = value) }
    fun updateGuestName(value: String) { _formState.value = _formState.value.copy(guestName = value) }
    fun updateGuestPhone(value: String) { _formState.value = _formState.value.copy(guestPhone = value) }
    fun updateGuestEmail(value: String) { _formState.value = _formState.value.copy(guestEmail = value) }
    fun updateNumberOfGuests(value: String) { _formState.value = _formState.value.copy(numberOfGuests = value) }
    fun updateTotalRevenue(value: String) { _formState.value = _formState.value.copy(totalRevenue = value) }
    fun updateReservationStatus(value: String) { _formState.value = _formState.value.copy(reservationStatus = value) }
    fun updatePaymentStatus(value: String) { _formState.value = _formState.value.copy(paymentStatus = value) }
}

data class ReservationFormState(
    val propertyId: String = "",
    val platform: String = "Airbnb",
    val reservationCode: String = "",
    val checkInDate: String = "",
    val checkOutDate: String = "",
    val guestName: String = "",
    val guestPhone: String = "",
    val guestEmail: String = "",
    val numberOfGuests: String = "",
    val totalRevenue: String = "",
    val reservationStatus: String = "Confirmada",
    val paymentStatus: String = "Pendente"
) {
    val isValid: Boolean
        get() = propertyId.isNotBlank() &&
                platform.isNotBlank() &&
                reservationCode.isNotBlank() &&
                checkInDate.isNotBlank() &&
                checkOutDate.isNotBlank() &&
                totalRevenue.isNotBlank() &&
                reservationStatus.isNotBlank()
}

sealed class ReservationFormUiState {
    object Idle : ReservationFormUiState()
    object Loading : ReservationFormUiState()
    object Saving : ReservationFormUiState()
    object Success : ReservationFormUiState()
    data class Error(val message: String) : ReservationFormUiState()
}
