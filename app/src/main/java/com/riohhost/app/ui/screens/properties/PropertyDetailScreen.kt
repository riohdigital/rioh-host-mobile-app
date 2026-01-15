package com.riohhost.app.ui.screens.properties

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.riohhost.app.utils.CurrencyUtils
import com.riohhost.app.utils.DateUtils
import com.riohhost.app.data.models.Reservation
import com.riohhost.app.data.models.PropertyCleaner

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PropertyDetailScreen(
    propertyId: String?,
    viewModel: PropertyDetailViewModel = viewModel(),
    onNavigateBack: () -> Unit,
    onEditClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val property = uiState.property
    val uriHandler = LocalUriHandler.current

    LaunchedEffect(propertyId) {
        if (propertyId != null) {
            viewModel.loadProperty(propertyId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(property?.nickname ?: "Detalhes do Imóvel") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    IconButton(onClick = { propertyId?.let { onEditClick(it) } }) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar")
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (property == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(uiState.error ?: "Propriedade não encontrada")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Header Info
                item {
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp)) {
                            Text(property.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Badge(property.propertyType ?: "Tipo N/A", MaterialTheme.colorScheme.secondaryContainer)
                                Badge(property.status ?: "Status N/A", if (property.status == "Ativo") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(property.address ?: "Endereço não informado", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                // 2. KPIs
                item {
                    Text("KPIs DO MÊS ATUAL", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        KpiCard("Ocupação", "%.1f%%".format(uiState.occupancyRate), Icons.Default.TrendingUp, Modifier.weight(1f))
                        KpiCard("Receita", CurrencyUtils.formatBRL(uiState.totalRevenue), Icons.Default.AttachMoney, Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        KpiCard("Reservas", uiState.reservationCount.toString(), Icons.Default.Event, Modifier.weight(1f))
                        KpiCard("Ticket Médio", CurrencyUtils.formatBRL(uiState.avgTicket), Icons.Default.AttachMoney, Modifier.weight(1f))
                    }
                }

                // 3. Configurations
                item {
                    SectionHeader("CONFIGURAÇÕES")
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp)) {
                            ConfigRow("Diária Base", CurrencyUtils.formatBRL(property.baseNightlyPrice ?: 0.0))
                            Divider(Modifier.padding(vertical = 4.dp))
                            ConfigRow("Taxa de Limpeza", CurrencyUtils.formatBRL(property.cleaningFee ?: 0.0))
                            ConfigRow("Comissão", "${((property.commissionRate ?: 0.0) * 100).toInt()}%")
                            Divider(Modifier.padding(vertical = 4.dp))
                            ConfigRow("Check-in / Out", "${property.defaultCheckinTime?.take(5) ?: "15:00"} / ${property.defaultCheckoutTime?.take(5) ?: "11:00"}")
                            ConfigRow("Capacidade", "${property.maxGuests ?: 0} hóspedes")
                        }
                    }
                }

                // 4. Next Reservations
                item {
                    SectionHeader("PRÓXIMAS RESERVAS")
                    if (uiState.nextReservations.isEmpty()) {
                        Text("Nenhuma reserva futura confirmada.", style = MaterialTheme.typography.bodySmall)
                    } else {
                        uiState.nextReservations.forEach { res ->
                            ReservationCompactItem(res)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        // Botão "Ver todas" could go here
                    }
                }

                // 5. Cleaners
                item {
                    SectionHeader("FAXINEIRAS VINCULADAS")
                    if (uiState.cleaners.isEmpty()) {
                        Text("Nenhuma faxineira vinculada.", style = MaterialTheme.typography.bodySmall)
                    } else {
                        uiState.cleaners.forEach { cleaner ->
                            CleanerCompactItem(cleaner)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }

                // 6. External Links
                item {
                    SectionHeader("LINKS EXTERNOS")
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { property.airbnbLink?.let { uriHandler.openUri(it) } },
                            enabled = !property.airbnbLink.isNullOrBlank(),
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5A5F)) // Airbnb Color
                        ) { Text("Airbnb") }
                        
                        Button(
                            onClick = { property.bookingLink?.let { uriHandler.openUri(it) } },
                            enabled = !property.bookingLink.isNullOrBlank(),
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF003580)) // Booking Color
                        ) { Text("Booking") }
                    }
                }

                // 7. Notes
                item {
                    SectionHeader("ANOTAÇÕES")
                    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                        Text(
                            text = if (!property.notes.isNullOrBlank()) "\"${property.notes}\"" else "Sem anotações.",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
fun KpiCard(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier) {
    Card(modifier) {
        Column(
            Modifier.padding(12.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
fun ReservationCompactItem(reservation: Reservation) {
    Card(Modifier.fillMaxWidth()) {
        Row(
            Modifier.padding(12.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(DateUtils.formatIsoToBrazilian(reservation.checkInDate), fontWeight = FontWeight.Bold)
                Text(reservation.guestName ?: "Sem nome", style = MaterialTheme.typography.bodyMedium)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(reservation.platform ?: "Direto", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                Text(CurrencyUtils.formatBRL(reservation.totalRevenue ?: 0.0), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun CleanerCompactItem(cleaner: PropertyCleaner) {
    Card(Modifier.fillMaxWidth()) {
        Row(
            Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Person, contentDescription = null)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(cleaner.fullName ?: "Sem nome", fontWeight = FontWeight.Bold)
                if (!cleaner.phone.isNullOrBlank()) {
                    Text(cleaner.phone, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
fun ConfigRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun Badge(text: String, color: Color) {
    Surface(
        color = color,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall
        )
    }
}
