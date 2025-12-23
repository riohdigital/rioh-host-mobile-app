package com.riohhost.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.riohhost.app.utils.DateRangeCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate

/**
 * Shared ViewModel for global filters used across the app.
 * Matches the React Context pattern from GlobalFiltersContext.tsx
 */
class GlobalFiltersViewModel : ViewModel() {
    
    private val _selectedProperties = MutableStateFlow<List<String>>(listOf("todas"))
    val selectedProperties: StateFlow<List<String>> = _selectedProperties.asStateFlow()
    
    private val _selectedPeriod = MutableStateFlow(DateRangeCalculator.Periods.CURRENT_YEAR)
    val selectedPeriod: StateFlow<String> = _selectedPeriod.asStateFlow()
    
    private val _selectedPlatform = MutableStateFlow("all")
    val selectedPlatform: StateFlow<String> = _selectedPlatform.asStateFlow()
    
    private val _customStartDate = MutableStateFlow<LocalDate?>(null)
    val customStartDate: StateFlow<LocalDate?> = _customStartDate.asStateFlow()
    
    private val _customEndDate = MutableStateFlow<LocalDate?>(null)
    val customEndDate: StateFlow<LocalDate?> = _customEndDate.asStateFlow()
    
    val dateRange: StateFlow<Pair<LocalDate, LocalDate>> = combine(
        _selectedPeriod, _customStartDate, _customEndDate
    ) { period, start, end ->
        DateRangeCalculator.calculate(period, start, end)
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        DateRangeCalculator.calculate(DateRangeCalculator.Periods.CURRENT_YEAR)
    )
    
    val dateRangeStrings: StateFlow<Pair<String, String>> = combine(
        dateRange
    ) { (range) ->
        Pair(
            DateRangeCalculator.toIsoString(range.first),
            DateRangeCalculator.toIsoString(range.second)
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        getDefaultDateRangeStrings()
    )
    
    fun setSelectedProperties(properties: List<String>) {
        android.util.Log.d("GlobalFilters", "Properties changed: $properties")
        _selectedProperties.value = properties
    }
    
    fun setSelectedPeriod(period: String) {
        android.util.Log.d("GlobalFilters", "Period changed: $period")
        _selectedPeriod.value = period
    }
    
    fun setSelectedPlatform(platform: String) {
        android.util.Log.d("GlobalFilters", "Platform changed: $platform")
        _selectedPlatform.value = platform
    }
    
    fun setCustomDateRange(start: LocalDate, end: LocalDate) {
        android.util.Log.d("GlobalFilters", "Custom range: $start to $end")
        _customStartDate.value = start
        _customEndDate.value = end
        _selectedPeriod.value = DateRangeCalculator.Periods.CUSTOM
    }
    
    fun resetFilters() {
        _selectedProperties.value = listOf("todas")
        _selectedPeriod.value = DateRangeCalculator.Periods.CURRENT_YEAR
        _selectedPlatform.value = "all"
        _customStartDate.value = null
        _customEndDate.value = null
    }
    
    fun isPropertySelected(propertyId: String): Boolean {
        val selected = _selectedProperties.value
        return selected.contains("todas") || selected.contains(propertyId)
    }
    
    fun getPropertyFilter(): List<String>? {
        val selected = _selectedProperties.value
        return if (selected.contains("todas")) null else selected
    }
    
    fun getPlatformFilter(): String? {
        val platform = _selectedPlatform.value
        return if (platform == "all") null else platform
    }
    
    private fun getDefaultDateRangeStrings(): Pair<String, String> {
        val range = DateRangeCalculator.calculate(DateRangeCalculator.Periods.CURRENT_YEAR)
        return Pair(
            DateRangeCalculator.toIsoString(range.first),
            DateRangeCalculator.toIsoString(range.second)
        )
    }
    
    companion object {
        val PLATFORMS = listOf(
            "all" to "Todas",
            "Airbnb" to "Airbnb",
            "Booking.com" to "Booking.com",
            "Direto" to "Direto",
            "VRBO" to "VRBO",
            "Hospedagem.com" to "Hospedagem.com"
        )
    }
}
