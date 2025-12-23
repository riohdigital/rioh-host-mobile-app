package com.riohhost.app.ui.screens.reservations

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.asStateFlow
import com.riohhost.app.data.models.Reservation
import com.riohhost.app.utils.CurrencyUtils

import androidx.compose.material3.ExperimentalMaterial3Api

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservationDetailScreen(
    reservationId: String?,
    viewModel: com.riohhost.app.ui.screens.reservations.ReservationDetailViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onNavigateBack: () -> Unit
) {
    val reservation by viewModel.reservation.collectAsState()
    
    LaunchedEffect(reservationId) {
        if (reservationId != null) {
            viewModel.loadReservation(reservationId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (reservation?.reservationCode != null) "Reserva #${reservation?.reservationCode}" else "Detalhes") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { padding ->
        if (reservation == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Status", style = MaterialTheme.typography.labelMedium)
                        Text(
                            text = reservation!!.reservationStatus?.uppercase() ?: "CONFIRMADO",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Limpeza: ${reservation!!.cleaningStatus ?: "Pendente"}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("Hóspede", style = MaterialTheme.typography.titleMedium)
                Card(Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, contentDescription = null)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(reservation!!.guestName ?: "Não informado", fontWeight = FontWeight.Bold)
                            if (reservation!!.guestEmail != null) {
                                Text(reservation!!.guestEmail!!, style = MaterialTheme.typography.bodySmall)
                            }
                            if (reservation!!.guestPhone != null) {
                                Text(reservation!!.guestPhone!!, style = MaterialTheme.typography.bodySmall)
                            }
                            Text("${reservation!!.numberOfGuests ?: 1} Hóspedes", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Card(Modifier.weight(1f)) {
                        Column(Modifier.padding(16.dp)) {
                            Text("Check-in", style = MaterialTheme.typography.labelSmall)
                            Text(reservation!!.checkInDate ?: "", fontWeight = FontWeight.Bold)
                        }
                    }
                    Card(Modifier.weight(1f)) {
                        Column(Modifier.padding(16.dp)) {
                            Text("Check-out", style = MaterialTheme.typography.labelSmall)
                            Text(reservation!!.checkOutDate ?: "", fontWeight = FontWeight.Bold)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Financeiro", style = MaterialTheme.typography.titleSmall)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total")
                            Text(
                                CurrencyUtils.formatBRL(reservation!!.totalRevenue?.toDoubleOrNull() ?: 0.0),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        if (reservation!!.cleaningFee != null) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Taxa de Limpeza", style = MaterialTheme.typography.bodySmall)
                                Text(CurrencyUtils.formatBRL(reservation!!.cleaningFee?.toDoubleOrNull() ?: 0.0), style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.weight(1f))
            
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { /* TODO: Contact via WhatsApp/Email */ },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Contatar")
                    }
                }
            }
        }
    }
}

class ReservationDetailViewModel(
    private val repository: com.riohhost.app.data.repositories.ReservationRepository = com.riohhost.app.data.repositories.ReservationRepository()
) : androidx.lifecycle.ViewModel() {
    private val _reservation = kotlinx.coroutines.flow.MutableStateFlow<Reservation?>(null)
    val reservation = _reservation.asStateFlow()

    fun loadReservation(id: String) {
        viewModelScope.launch {
            _reservation.value = repository.getReservationById(id)
        }
    }
}
