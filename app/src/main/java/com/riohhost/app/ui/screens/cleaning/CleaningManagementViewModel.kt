package com.riohhost.app.ui.screens.cleaning

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.riohhost.app.data.models.CleanerProfile
import com.riohhost.app.data.models.CleaningReservation
import com.riohhost.app.data.repositories.CleaningRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CleaningManagementViewModel : ViewModel() {
    private val cleaningRepository = CleaningRepository()

    private val _uiState = MutableStateFlow<CleaningUiState>(CleaningUiState.Loading)
    val uiState: StateFlow<CleaningUiState> = _uiState.asStateFlow()

    private val _allCleanings = MutableStateFlow<List<CleaningReservation>>(emptyList())
    val allCleanings: StateFlow<List<CleaningReservation>> = _allCleanings.asStateFlow()

    private val _availableCleanings = MutableStateFlow<List<CleaningReservation>>(emptyList())
    val availableCleanings: StateFlow<List<CleaningReservation>> = _availableCleanings.asStateFlow()

    private val _cleaners = MutableStateFlow<List<CleanerProfile>>(emptyList())
    val cleaners: StateFlow<List<CleanerProfile>> = _cleaners.asStateFlow()

    private val _stats = MutableStateFlow(CleaningStats())
    val stats: StateFlow<CleaningStats> = _stats.asStateFlow()

    init {
        loadData()
    }

    fun loadData(startDate: String? = null, endDate: String? = null) {
        viewModelScope.launch {
            _uiState.value = CleaningUiState.Loading
            
            try {
                // Load all assigned cleanings
                val allResult = cleaningRepository.getAllCleanerReservations(startDate, endDate)
                val all = allResult.getOrDefault(emptyList())
                _allCleanings.value = all
                
                // Load available cleanings
                val availableResult = cleaningRepository.getAvailableReservations(startDate, endDate)
                val available = availableResult.getOrDefault(emptyList())
                _availableCleanings.value = available
                
                // Load cleaners
                val cleanersResult = cleaningRepository.getCleanersForProperties()
                _cleaners.value = cleanersResult.getOrDefault(emptyList())
                
                // Calculate stats
                _stats.value = CleaningStats(
                    totalCleanings = all.size,
                    pendingCleanings = all.count { it.cleaningStatus?.equals("Pendente", ignoreCase = true) == true },
                    completedCleanings = all.count { it.cleaningStatus?.equals("Realizada", ignoreCase = true) == true },
                    availableCleanings = available.size
                )
                
                _uiState.value = CleaningUiState.Success
            } catch (e: Exception) {
                android.util.Log.e("CleaningVM", "Erro ao carregar dados: ${e.message}", e)
                _uiState.value = CleaningUiState.Error(e.message ?: "Erro ao carregar dados")
            }
        }
    }

    fun assignCleaning(reservationId: String, cleanerId: String) {
        viewModelScope.launch {
            val result = cleaningRepository.assignCleaning(reservationId, cleanerId)
            if (result.isSuccess) {
                loadData() // Reload data
            }
        }
    }

    fun unassignCleaning(reservationId: String) {
        viewModelScope.launch {
            val result = cleaningRepository.unassignCleaning(reservationId)
            if (result.isSuccess) {
                loadData() // Reload data
            }
        }
    }

    fun toggleCleaningStatus(reservationId: String) {
        viewModelScope.launch {
            val result = cleaningRepository.toggleCleaningStatus(reservationId)
            if (result.isSuccess) {
                loadData() // Reload data
            }
        }
    }

    // Filtered lists
    val pendingCleanings: List<CleaningReservation>
        get() = _allCleanings.value.filter { it.cleaningStatus?.equals("Pendente", ignoreCase = true) == true }
    
    val completedCleanings: List<CleaningReservation>
        get() = _allCleanings.value.filter { it.cleaningStatus?.equals("Realizada", ignoreCase = true) == true }
}

data class CleaningStats(
    val totalCleanings: Int = 0,
    val pendingCleanings: Int = 0,
    val completedCleanings: Int = 0,
    val availableCleanings: Int = 0
)

sealed class CleaningUiState {
    object Loading : CleaningUiState()
    object Success : CleaningUiState()
    data class Error(val message: String) : CleaningUiState()
}
