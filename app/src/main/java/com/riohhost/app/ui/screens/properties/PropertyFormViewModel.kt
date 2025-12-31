package com.riohhost.app.ui.screens.properties

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.riohhost.app.data.models.Property
import com.riohhost.app.data.models.PropertyFormData
import com.riohhost.app.data.repositories.PropertyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Reusing FormUiState from ReservationForm if possible, or redefine
sealed class PropFormUiState {
    object Idle : PropFormUiState()
    object Loading : PropFormUiState()
    object Success : PropFormUiState()
    data class Error(val message: String) : PropFormUiState()
}

class PropertyFormViewModel : ViewModel() {
    private val repository = PropertyRepository()

    private val _uiState = MutableStateFlow<PropFormUiState>(PropFormUiState.Idle)
    val uiState: StateFlow<PropFormUiState> = _uiState.asStateFlow()

    private val _existingProperty = MutableStateFlow<Property?>(null)
    val existingProperty: StateFlow<Property?> = _existingProperty.asStateFlow()

    fun loadProperty(id: String) {
        viewModelScope.launch {
            _uiState.value = PropFormUiState.Loading
            val result = repository.getPropertyById(id)
            if (result != null) {
                _existingProperty.value = result
                _uiState.value = PropFormUiState.Idle
            } else {
                _uiState.value = PropFormUiState.Error("Propriedade não encontrada")
            }
        }
    }

    fun submitForm(isEdit: Boolean, propertyId: String?, data: PropertyFormData, userId: String) {
        viewModelScope.launch {
            _uiState.value = PropFormUiState.Loading
            val result = if (isEdit && propertyId != null) {
                repository.updateProperty(propertyId, data)
            } else {
                repository.createProperty(data, userId)
            }
            
            result.onSuccess { 
                _uiState.value = PropFormUiState.Success 
            }.onFailure { e -> 
                _uiState.value = PropFormUiState.Error(e.message ?: "Erro ao salvar") 
            }
        }
    }
}
