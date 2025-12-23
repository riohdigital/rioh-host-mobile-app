package com.riohhost.app.ui.screens.properties

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.riohhost.app.data.models.PropertyCreate
import com.riohhost.app.data.models.PropertyUpdate
import com.riohhost.app.data.repositories.PropertyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PropertyFormViewModel : ViewModel() {
    private val propertyRepository = PropertyRepository()

    private val _uiState = MutableStateFlow<PropertyFormUiState>(PropertyFormUiState.Idle)
    val uiState: StateFlow<PropertyFormUiState> = _uiState.asStateFlow()

    private val _formState = MutableStateFlow(PropertyFormState())
    val formState: StateFlow<PropertyFormState> = _formState.asStateFlow()

    fun loadProperty(id: String) {
        viewModelScope.launch {
            _uiState.value = PropertyFormUiState.Loading
            val property = propertyRepository.getPropertyById(id)
            if (property != null) {
                _formState.value = PropertyFormState(
                    name = property.name,
                    nickname = property.nickname ?: "",
                    address = property.address ?: "",
                    propertyType = property.propertyType ?: "Apartamento",
                    status = property.status ?: "Ativo",
                    airbnbLink = property.airbnbLink ?: "",
                    bookingLink = property.bookingLink ?: "",
                    commissionRate = ((property.commissionRate ?: 0.20) * 100).toString(),
                    cleaningFee = property.cleaningFee?.toString() ?: "",
                    baseNightlyPrice = property.baseNightlyPrice?.toString() ?: "",
                    maxGuests = property.maxGuests?.toString() ?: "",
                    defaultCheckinTime = property.defaultCheckinTime ?: "15:00",
                    defaultCheckoutTime = property.defaultCheckoutTime ?: "11:00",
                    notes = property.notes ?: ""
                )
                _uiState.value = PropertyFormUiState.Idle
            } else {
                _uiState.value = PropertyFormUiState.Error("Propriedade não encontrada")
            }
        }
    }

    fun createProperty() {
        val form = _formState.value
        if (!form.isValid) {
            _uiState.value = PropertyFormUiState.Error("Preencha todos os campos obrigatórios")
            return
        }

        viewModelScope.launch {
            _uiState.value = PropertyFormUiState.Saving
            val property = PropertyCreate(
                name = form.name,
                nickname = form.nickname.ifBlank { null },
                address = form.address.ifBlank { null },
                propertyType = form.propertyType,
                status = form.status,
                airbnbLink = form.airbnbLink.ifBlank { null },
                bookingLink = form.bookingLink.ifBlank { null },
                commissionRate = (form.commissionRate.toDoubleOrNull() ?: 20.0) / 100,
                cleaningFee = form.cleaningFee.toDoubleOrNull(),
                baseNightlyPrice = form.baseNightlyPrice.toDoubleOrNull(),
                maxGuests = form.maxGuests.toIntOrNull(),
                defaultCheckinTime = form.defaultCheckinTime.ifBlank { "15:00" },
                defaultCheckoutTime = form.defaultCheckoutTime.ifBlank { "11:00" },
                notes = form.notes.ifBlank { null }
            )

            val result = propertyRepository.createProperty(property)
            if (result.isSuccess) {
                _uiState.value = PropertyFormUiState.Success
            } else {
                _uiState.value = PropertyFormUiState.Error(result.exceptionOrNull()?.message ?: "Erro ao criar")
            }
        }
    }

    fun updateProperty(id: String) {
        val form = _formState.value
        viewModelScope.launch {
            _uiState.value = PropertyFormUiState.Saving
            val updates = PropertyUpdate(
                name = form.name,
                nickname = form.nickname.ifBlank { null },
                address = form.address.ifBlank { null },
                propertyType = form.propertyType,
                status = form.status,
                airbnbLink = form.airbnbLink.ifBlank { null },
                bookingLink = form.bookingLink.ifBlank { null },
                commissionRate = (form.commissionRate.toDoubleOrNull() ?: 20.0) / 100,
                cleaningFee = form.cleaningFee.toDoubleOrNull(),
                baseNightlyPrice = form.baseNightlyPrice.toDoubleOrNull(),
                maxGuests = form.maxGuests.toIntOrNull(),
                defaultCheckinTime = form.defaultCheckinTime.ifBlank { null },
                defaultCheckoutTime = form.defaultCheckoutTime.ifBlank { null },
                notes = form.notes.ifBlank { null }
            )

            val result = propertyRepository.updateProperty(id, updates)
            if (result.isSuccess) {
                _uiState.value = PropertyFormUiState.Success
            } else {
                _uiState.value = PropertyFormUiState.Error(result.exceptionOrNull()?.message ?: "Erro ao atualizar")
            }
        }
    }

    // Form field updaters
    fun updateName(value: String) { _formState.value = _formState.value.copy(name = value) }
    fun updateNickname(value: String) { _formState.value = _formState.value.copy(nickname = value) }
    fun updateAddress(value: String) { _formState.value = _formState.value.copy(address = value) }
    fun updatePropertyType(value: String) { _formState.value = _formState.value.copy(propertyType = value) }
    fun updateStatus(value: String) { _formState.value = _formState.value.copy(status = value) }
    fun updateAirbnbLink(value: String) { _formState.value = _formState.value.copy(airbnbLink = value) }
    fun updateBookingLink(value: String) { _formState.value = _formState.value.copy(bookingLink = value) }
    fun updateCommissionRate(value: String) { _formState.value = _formState.value.copy(commissionRate = value) }
    fun updateCleaningFee(value: String) { _formState.value = _formState.value.copy(cleaningFee = value) }
    fun updateBaseNightlyPrice(value: String) { _formState.value = _formState.value.copy(baseNightlyPrice = value) }
    fun updateMaxGuests(value: String) { _formState.value = _formState.value.copy(maxGuests = value) }
    fun updateDefaultCheckinTime(value: String) { _formState.value = _formState.value.copy(defaultCheckinTime = value) }
    fun updateDefaultCheckoutTime(value: String) { _formState.value = _formState.value.copy(defaultCheckoutTime = value) }
    fun updateNotes(value: String) { _formState.value = _formState.value.copy(notes = value) }
}

data class PropertyFormState(
    val name: String = "",
    val nickname: String = "",
    val address: String = "",
    val propertyType: String = "Apartamento",
    val status: String = "Ativo",
    val airbnbLink: String = "",
    val bookingLink: String = "",
    val commissionRate: String = "20",
    val cleaningFee: String = "",
    val baseNightlyPrice: String = "",
    val maxGuests: String = "",
    val defaultCheckinTime: String = "15:00",
    val defaultCheckoutTime: String = "11:00",
    val notes: String = ""
) {
    val isValid: Boolean
        get() = name.isNotBlank() && propertyType.isNotBlank() && status.isNotBlank()
}

sealed class PropertyFormUiState {
    object Idle : PropertyFormUiState()
    object Loading : PropertyFormUiState()
    object Saving : PropertyFormUiState()
    object Success : PropertyFormUiState()
    data class Error(val message: String) : PropertyFormUiState()
}
