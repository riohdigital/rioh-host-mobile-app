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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.riohhost.app.data.models.Reservation
import com.riohhost.app.ui.theme.*
import com.riohhost.app.utils.CurrencyUtils
import com.riohhost.app.utils.DateUtils

@Composable
fun ReservationsListScreen(
    viewModel: ReservationViewModel = viewModel(),
    onReservationClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { /* TODO: Add Reservation */ }) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar Reserva")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                text = "Reservas",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { 
                    Text(
                        "Buscar por hóspede, código, imóvel...",
                        style = MaterialTheme.typography.bodyMedium
                    ) 
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Buscar",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                Icons.Default.Clear,
                                contentDescription = "Limpar",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() })
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
                    // Filter and sort reservations
                    val filteredAndSortedReservations by remember(state.reservations, searchQuery) {
                        derivedStateOf {
                            val query = searchQuery.trim().lowercase()
                            
                            // Filter by search query
                            val filtered = if (query.isEmpty()) {
                                state.reservations
                            } else {
                                state.reservations.filter { reservation ->
                                    // Search in guest name
                                    reservation.guestName?.lowercase()?.contains(query) == true ||
                                    // Search in reservation code/ID
                                    reservation.id.lowercase().contains(query) ||
                                    // Search in property name/nickname
                                    reservation.propertyId?.lowercase()?.contains(query) == true ||
                                    // Search in platform
                                    reservation.platform?.lowercase()?.contains(query) == true ||
                                    // Search in reservation code if available
                                    reservation.reservationCode?.lowercase()?.contains(query) == true
                                }
                            }
                            
                            // Sort by check-in date descending (most recent first)
                            filtered.sortedWith { r1, r2 ->
                                DateUtils.compareIsoDateStringsDescending(
                                    r1.checkInDate,
                                    r2.checkInDate
                                )
                            }
                        }
                    }
                    
                    if (filteredAndSortedReservations.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = if (searchQuery.isNotEmpty()) 
                                        "Nenhuma reserva encontrada para \"$searchQuery\"" 
                                    else 
                                        "Nenhuma reserva encontrada",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (searchQuery.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Tente outro termo de busca",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    } else {
                        // Show result count when searching
                        if (searchQuery.isNotEmpty()) {
                            Text(
                                text = "${filteredAndSortedReservations.size} resultado(s) encontrado(s)",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(filteredAndSortedReservations) { reservation ->
                                ReservationItemCard(
                                    reservation = reservation, 
                                    onClick = { onReservationClick(reservation.id) }
                                )
                            }
                            
                            // Bottom spacing for FAB
                            item {
                                Spacer(modifier = Modifier.height(80.dp))
                            }
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
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
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
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(platformColor, CircleShape)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            
            // Use Brazilian date format
            Text(
                text = "${DateUtils.formatIsoToBrazilian(reservation.checkInDate)} → ${DateUtils.formatIsoToBrazilian(reservation.checkOutDate)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
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
