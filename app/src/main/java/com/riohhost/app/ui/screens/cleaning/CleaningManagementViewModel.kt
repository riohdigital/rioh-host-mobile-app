package com.riohhost.app.ui.screens.cleaning

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.riohhost.app.data.models.CleanerStats
import com.riohhost.app.data.models.ReservationWithCleanerInfo
import com.riohhost.app.data.repositories.CleaningRepository
import com.riohhost.app.ui.screens.cleaning.models.CleaningTab
import com.riohhost.app.ui.screens.cleaning.models.CleaningUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.riohhost.app.data.models.CleaningCleanerProfile

class CleaningManagementViewModel(
    private val repository: CleaningRepository = CleaningRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(CleaningUiState())
    val uiState: StateFlow<CleaningUiState> = _uiState.asStateFlow()

    init {
        loadData()
        checkPermissions()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                // Fetch for current month + next month for context, or a wider range
                // For now, let's hardcode a reasonable range or calculate it dynamically
                val today = java.time.LocalDate.now()
                val startDate = today.minusDays(7).toString() // 1 week back
                val endDate = today.plusMonths(2).toString()  // 2 months forward

                // Parallel fetch
                val assignedDeferred =  repository.getAllCleanerReservations(startDate, endDate, null)
                val availableDeferred = repository.getAllAvailableReservations(startDate, endDate, null)
                val cleanersDeferred = repository.getCleanersForProperties(null)

                val assigned = assignedDeferred
                val available = availableDeferred
                val cleaners = cleanersDeferred

                val stats = calculateCleanerStats(assigned)

                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        allCleanings = assigned,
                        availableCleanings = available,
                        cleaners = cleaners,
                        cleanerStats = stats
                    )
                }
                applyFilters() // Apply initial filters to set displayedCleanings
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    private fun checkPermissions() {
        viewModelScope.launch {
            val canAssign = repository.hasPermission("gestao_faxinas_assign")
            val canReassign = repository.hasPermission("gestao_faxinas_reassign")
            val canManage = repository.hasPermission("gestao_faxinas_manage")
            
            _uiState.update { 
                it.copy(
                    canAssign = canAssign,
                    canReassign = canReassign,
                    canManageStatus = canManage
                ) 
            }
        }
    }

    fun onTabSelected(tab: CleaningTab) {
        _uiState.update { it.copy(selectedTab = tab) }
        applyFilters()
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        applyFilters()
    }

    fun onCleanerFilterSelected(cleanerId: String?) {
        _uiState.update { it.copy(selectedCleanerFilter = cleanerId) }
        applyFilters()
    }
    
    fun onRefresh() {
        loadData()
    }

    private fun applyFilters() {
        val currentState = _uiState.value
        val tab = currentState.selectedTab
        val query = currentState.searchQuery.lowercase()
        val cleanerId = currentState.selectedCleanerFilter

        // Choose source list based on tab
        var listToFilter = if (tab == CleaningTab.AVAILABLE) {
            currentState.availableCleanings
        } else {
            currentState.allCleanings
        }

        // Filter by Tab Status
        if (tab == CleaningTab.PENDING) {
            listToFilter = listToFilter.filter { it.cleaning_status == "Pendente" }
        } else if (tab == CleaningTab.COMPLETED) {
            listToFilter = listToFilter.filter { it.cleaning_status == "Realizada" }
        }

        // Filter by Cleaner
        if (cleanerId != null && tab != CleaningTab.AVAILABLE) { // Available items don't have cleaners usually
             listToFilter = listToFilter.filter { it.cleaner_user_id == cleanerId }
        }

        // Filter by Search
        if (query.isNotEmpty()) {
            listToFilter = listToFilter.filter { item ->
                item.properties?.name?.lowercase()?.contains(query) == true ||
                item.reservation_code.lowercase().contains(query) ||
                item.guest_name?.lowercase()?.contains(query) == true ||
                item.cleaner_info?.full_name?.lowercase()?.contains(query) == true
            }
        }
        
        // Sort: Urgent first (based on checkout date close to today), then by check-out date
        listToFilter = listToFilter.sortedBy { it.check_out_date }

        _uiState.update { it.copy(displayedCleanings = listToFilter) }
    }

    private fun calculateCleanerStats(cleanings: List<ReservationWithCleanerInfo>): Map<String, CleanerStats> {
        return cleanings
            .filter { it.cleaner_user_id != null && it.cleaner_info != null }
            .groupBy { it.cleaner_user_id!! }
            .mapValues { (_, reservations) ->
                val first = reservations.first()
                CleanerStats(
                    name = first.cleaner_info!!.full_name,
                    total = reservations.size,
                    pending = reservations.count { it.cleaning_status == "Pendente" },
                    completed = reservations.count { it.cleaning_status == "Realizada" }
                )
            }
    }

    // Actions
    fun assignCleaner(reservationId: String, cleanerId: String) {
        viewModelScope.launch {
            val result = repository.assignCleaning(reservationId, cleanerId)
            if (result.isSuccess) {
                loadData() // Refresh on success
            } else {
                 _uiState.update { it.copy(errorMessage = "Erro ao atribuir faxineira") }
            }
        }
    }

    fun reassignCleaner(reservationId: String, cleanerId: String) {
        viewModelScope.launch {
            val result = repository.reassignCleaning(reservationId, cleanerId)
            if (result.isSuccess) {
                loadData()
            } else {
                _uiState.update { it.copy(errorMessage = "Erro ao reatribuir faxineira") }
            }
        }
    }

    fun unassignCleaner(reservationId: String) {
        viewModelScope.launch {
            val result = repository.unassignCleaning(reservationId)
            if (result.isSuccess) {
                loadData()
            } else {
                _uiState.update { it.copy(errorMessage = "Erro ao remover faxineira") }
            }
        }
    }

    fun toggleStatus(reservationId: String) {
        viewModelScope.launch {
            val result = repository.toggleCleaningStatus(reservationId)
            if (result.isSuccess) {
                loadData()
            } else {
                _uiState.update { it.copy(errorMessage = "Erro ao alterar status") }
            }
        }
    }
}
