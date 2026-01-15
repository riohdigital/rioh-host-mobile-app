package com.riohhost.app.ui.screens.cleaning.models

import com.riohhost.app.data.models.CleaningCleanerProfile
import com.riohhost.app.data.models.CleanerStats
import com.riohhost.app.data.models.ReservationWithCleanerInfo

data class CleaningUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    
    // Data Lists
    val allCleanings: List<ReservationWithCleanerInfo> = emptyList(),
    val availableCleanings: List<ReservationWithCleanerInfo> = emptyList(),
    val cleaners: List<CleaningCleanerProfile> = emptyList(),
    
    // Filter State
    val selectedTab: CleaningTab = CleaningTab.ALL,
    val searchQuery: String = "",
    val selectedCleanerFilter: String? = null,
    
    // Computed/Derived Data (for UI display)
    val displayedCleanings: List<ReservationWithCleanerInfo> = emptyList(),
    val cleanerStats: Map<String, CleanerStats> = emptyMap(),
    
    // Permissions
    val canAssign: Boolean = false,
    val canReassign: Boolean = false,
    val canManageStatus: Boolean = false
)

enum class CleaningTab {
    ALL,
    PENDING,
    COMPLETED,
    AVAILABLE,
    STATS
}
