package com.riohhost.app.ui.screens.reservations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.riohhost.app.data.models.Reservation
import com.riohhost.app.data.repositories.ReservationRepository
import com.riohhost.app.ui.GlobalFiltersViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class ReservationViewModel : ViewModel() {
    private val reservationRepository = ReservationRepository()
    
    var globalFilters: GlobalFiltersViewModel? = null
        private set

    private val _uiState = MutableStateFlow<ReservationsUiState>(ReservationsUiState.Loading)
    val uiState: StateFlow<ReservationsUiState> = _uiState.asStateFlow()

    init {
        loadReservations()
    }
    
    fun setFilters(filters: GlobalFiltersViewModel) {
        if (globalFilters != filters) {
            globalFilters = filters
            observeFilterChanges()
            // Trigger refresh
            refresh()
        }
    }

    private fun observeFilterChanges() {
        viewModelScope.launch {
            globalFilters?.let { filters ->
                combine(
                    filters.dateRangeStrings,
                    filters.selectedProperties,
                    filters.selectedPlatform
                ) { dateRange, properties, platform ->
                    Triple(dateRange, properties, platform)
                }.collectLatest { (dateRange, properties, platform) ->
                    loadReservations(
                        startDate = dateRange.first,
                        endDate = dateRange.second,
                        propertyIds = if (properties.contains("todas")) null else properties,
                        platform = if (platform == "all") null else platform
                    )
                }
            }
        }
    }
    
    fun refresh() {
        if (globalFilters != null) {
            val dateRange = globalFilters!!.dateRangeStrings.value
            loadReservations(
                startDate = dateRange.first,
                endDate = dateRange.second,
                propertyIds = globalFilters!!.getPropertyFilter(),
                platform = globalFilters!!.getPlatformFilter()
            )
        } else {
            loadReservations()
        }
    }

    private fun loadReservations(
        startDate: String? = null,
        endDate: String? = null,
        propertyIds: List<String>? = null,
        platform: String? = null
    ) {
        viewModelScope.launch {
            _uiState.value = ReservationsUiState.Loading
            try {
                val reservations = if (startDate != null && endDate != null) {
                    reservationRepository.getReservationsFiltered(startDate, endDate, propertyIds, platform)
                } else {
                    reservationRepository.getReservations()
                }
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
