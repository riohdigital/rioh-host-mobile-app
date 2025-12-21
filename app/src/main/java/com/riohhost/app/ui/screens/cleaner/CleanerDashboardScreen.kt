package com.riohhost.app.ui.screens.cleaner

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.riohhost.app.data.models.Reservation

@Composable
fun CleanerDashboardScreen(
    viewModel: CleanerViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Minhas Faxinas") })
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Disponíveis") })
        }

        when (val state = uiState) {
            is CleanerUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is CleanerUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = state.message, color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { viewModel.loadCleanerData() }) {
                        Text("Tentar Novamente")
                    }
                }
            }
            is CleanerUiState.Success -> {
                val list = if (selectedTab == 0) state.assignedReservations else state.availableReservations
                
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(list) { reservation ->
                        CleaningCard(
                            reservation = reservation,
                            isAvailableTab = selectedTab == 1,
                            onAccept = { viewModel.acceptCleaning(reservation.id) },
                            onToggleStatus = { viewModel.toggleStatus(reservation.id) }
                        )
                    }
                    if (list.isEmpty()) {
                        item {
                            Text(
                                text = "Nenhuma faxina encontrada.",
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CleaningCard(
    reservation: Reservation,
    isAvailableTab: Boolean,
    onAccept: () -> Unit,
    onToggleStatus: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Reserva: ${reservation.reservationCode ?: reservation.id.take(8)}",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Saída: ${reservation.checkOutDate}")
            Text(text = "Status: ${reservation.cleaningStatus ?: "Pendente"}")
            
            Spacer(modifier = Modifier.height(8.dp))
            if (isAvailableTab) {
                Button(onClick = onAccept, modifier = Modifier.fillMaxWidth()) {
                    Text("Aceitar Faxina")
                }
            } else {
                Button(onClick = onToggleStatus, modifier = Modifier.fillMaxWidth()) {
                    val label = if (reservation.cleaningStatus == "completed") "Reabrir" else "Concluir"
                    Text(label)
                }
            }
        }
    }
}
