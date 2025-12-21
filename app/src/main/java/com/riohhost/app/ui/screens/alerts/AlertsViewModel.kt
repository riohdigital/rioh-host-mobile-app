package com.riohhost.app.ui.screens.alerts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.riohhost.app.data.models.NotificationDestination
import com.riohhost.app.data.repositories.AlertsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AlertsViewModel(
    private val repository: AlertsRepository = AlertsRepository()
) : ViewModel() {

    private val _destinations = MutableStateFlow<List<NotificationDestination>>(emptyList())
    val destinations = _destinations.asStateFlow()

    init {
        loadDestinations()
    }

    private fun loadDestinations() {
        viewModelScope.launch {
            _destinations.value = repository.getDestinations()
        }
    }
}
