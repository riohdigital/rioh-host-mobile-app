package com.riohhost.app.ui.screens.reservations

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservationDetailScreen(
    reservationId: String?,
    viewModel: ReservationDetailViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onNavigateBack: () -> Unit,
    onEditClick: (String) -> Unit = {}
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
                },
                actions = {
                    if (reservationId != null) {
                        IconButton(onClick = { onEditClick(reservationId) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (reservationId != null) {
                ExtendedFloatingActionButton(
                    onClick = { onEditClick(reservationId) },
                    icon = { Icon(Icons.Default.Edit, contentDescription = null) },
                    text = { Text("Editar") },
                    containerColor = MaterialTheme.colorScheme.primary
                )
            }
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
                // Status Header
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Status", style = MaterialTheme.typography.labelMedium)
                                Text(
                                    text = reservation!!.reservationStatus?.uppercase() ?: "CONFIRMADO",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            // Platform badge
                            val platformColor = when (reservation!!.platform?.lowercase()) {
                                "airbnb" -> Color(0xFFFF5A5F)
                                "booking", "booking.com" -> Color(0xFF003580)
                                else -> Color(0xFF4CAF50)
                            }
                            Surface(
                                color = platformColor,
                                shape = MaterialTheme.shapes.small
                            ) {
                                Text(
                                    text = reservation!!.platform ?: "Direto",
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Limpeza: ${reservation!!.cleaningStatus ?: "Pendente"}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Pagamento: ${reservation!!.paymentStatus ?: "Pendente"}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Guest Info
                Text("Hóspede", style = MaterialTheme.typography.titleMedium)
                Card(Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(40.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(reservation!!.guestName ?: "Não informado", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            if (!reservation!!.guestEmail.isNullOrBlank()) {
                                Text(reservation!!.guestEmail!!, style = MaterialTheme.typography.bodySmall)
                            }
                            if (!reservation!!.guestPhone.isNullOrBlank()) {
                                Text(reservation!!.guestPhone!!, style = MaterialTheme.typography.bodySmall)
                            }
                            Text("${reservation!!.numberOfGuests ?: 1} Hóspede(s)", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Dates
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Card(Modifier.weight(1f)) {
                        Column(Modifier.padding(16.dp)) {
                            Text("Check-in", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                            Text(reservation!!.checkInDate ?: "", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Text(reservation!!.checkinTime?.take(5) ?: "--:--", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    Card(Modifier.weight(1f)) {
                        Column(Modifier.padding(16.dp)) {
                            Text("Check-out", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                            Text(reservation!!.checkOutDate ?: "", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Text(reservation!!.checkoutTime?.take(5) ?: "--:--", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Financial
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Financeiro", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Bruto")
                            Text(
                                CurrencyUtils.formatBRL(reservation!!.totalRevenue ?: 0.0),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        if ((reservation!!.cleaningFee ?: 0.0) > 0) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Taxa de Limpeza", style = MaterialTheme.typography.bodySmall)
                                Text(CurrencyUtils.formatBRL(reservation!!.cleaningFee ?: 0.0), style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        if ((reservation!!.commissionAmount ?: 0.0) > 0) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Comissão", style = MaterialTheme.typography.bodySmall)
                                Text(CurrencyUtils.formatBRL(reservation!!.commissionAmount ?: 0.0), style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Líquido", fontWeight = FontWeight.Bold)
                            Text(
                                CurrencyUtils.formatBRL(reservation!!.netRevenue ?: 0.0),
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4CAF50)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(80.dp)) // Space for FAB
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
