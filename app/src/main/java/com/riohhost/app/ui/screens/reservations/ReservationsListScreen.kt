package com.riohhost.app.ui.screens.reservations

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.riohhost.app.data.models.Reservation
import com.riohhost.app.ui.GlobalFiltersViewModel
import com.riohhost.app.ui.theme.*
import com.riohhost.app.utils.CurrencyUtils

@Composable
fun ReservationsListScreen(
    filtersViewModel: GlobalFiltersViewModel = viewModel(),
    viewModel: ReservationViewModel = viewModel(),
    onReservationClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val dateRange by filtersViewModel.dateRangeStrings.collectAsState()
    val selectedProperties by filtersViewModel.selectedProperties.collectAsState()
    val selectedPlatform by filtersViewModel.selectedPlatform.collectAsState()

    // Reload when filters change
    LaunchedEffect(dateRange, selectedProperties, selectedPlatform) {
        viewModel.loadReservationsFiltered(
            startDate = dateRange.first,
            endDate = dateRange.second,
            propertyIds = filtersViewModel.getPropertyFilter(),
            platform = filtersViewModel.getPlatformFilter()
        )
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Reservas",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))

        when (val state = uiState) {
            is ReservationsUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is ReservationsUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = state.message, color = MaterialTheme.colorScheme.error)
                }
            }
            is ReservationsUiState.Success -> {
                if (state.reservations.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = "Nenhuma reserva encontrada para este período.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(state.reservations) { reservation ->
                            ReservationItemCard(reservation, onClick = { onReservationClick(reservation.id) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReservationItemCard(reservation: Reservation, onClick: () -> Unit) {
    val platformColor = when (reservation.platform?.lowercase()) {
        "airbnb" -> AirbnbColor
        "booking" -> BookingColor
        else -> DirectColor
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = reservation.guestName ?: "Hóspede",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Box(modifier = Modifier.size(12.dp).background(platformColor, CircleShape))
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${reservation.checkInDate} -> ${reservation.checkOutDate}",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = CurrencyUtils.formatBRL(reservation.totalRevenue ?: 0.0),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = reservation.cleaningStatus ?: "Pendente",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
