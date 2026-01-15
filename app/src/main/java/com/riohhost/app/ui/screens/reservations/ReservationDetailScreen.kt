package com.riohhost.app.ui.screens.reservations

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
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
import kotlinx.coroutines.flow.map
import com.riohhost.app.data.models.Reservation
import com.riohhost.app.utils.CurrencyUtils
import com.riohhost.app.utils.DateUtils

import androidx.compose.material3.ExperimentalMaterial3Api

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservationDetailScreen(
    reservationId: String?,
    viewModel: com.riohhost.app.ui.screens.reservations.ReservationDetailViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onNavigateBack: () -> Unit,
    onEditClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val reservation = uiState.reservation
    val property = uiState.property
    val cleaner = uiState.cleaner
    
    LaunchedEffect(reservationId) {
        if (reservationId != null) {
            viewModel.loadReservation(reservationId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(if (reservation?.reservationCode != null) "Reserva #${reservation?.reservationCode}" else "Detalhes") 
                        if (property != null) {
                            Text(property.name, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    IconButton(onClick = { reservationId?.let { onEditClick(it) } }) {
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
        } else if (reservation == null) {
             Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Reserva não encontrada")
            }
        } else {
            androidx.compose.foundation.lazy.LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Header with Badges
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                         Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Badge(reservation.platform ?: "Direto", MaterialTheme.colorScheme.tertiaryContainer)
                            Badge(reservation.reservationStatus?.uppercase() ?: "CONFIRMADA", 
                                if (reservation.reservationStatus == "cancelada") MaterialTheme.colorScheme.errorContainer 
                                else MaterialTheme.colorScheme.primaryContainer)
                        }
                    }
                }

                // 2. Guest Info
                item {
                    DetailSection(title = "HÓSPEDE") {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(reservation.guestName ?: "Sem nome", fontWeight = FontWeight.Bold)
                                Text("${reservation.numberOfGuests ?: 1} hóspedes", style = MaterialTheme.typography.bodyMedium)
                                
                                if (!reservation.guestPhone.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(reservation.guestPhone!!, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                                if (!reservation.guestEmail.isNullOrBlank()) {
                                    Text(reservation.guestEmail!!, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }

                // 3. Dates
                item {
                    DetailSection(title = "DATAS E HORÁRIOS") {
                         Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Check-in", style = MaterialTheme.typography.labelMedium)
                                Text(DateUtils.formatIsoToBrazilian(reservation.checkInDate), fontWeight = FontWeight.Bold)
                                Text(reservation.checkinTime?.take(5) ?: "15:00", style = MaterialTheme.typography.bodySmall)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Check-out", style = MaterialTheme.typography.labelMedium)
                                Text(DateUtils.formatIsoToBrazilian(reservation.checkOutDate), fontWeight = FontWeight.Bold)
                                Text(reservation.checkoutTime?.take(5) ?: "11:00", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        // Duration could be calculated here using ChronoUnit if needed, but keeping it simple for now
                    }
                }

                // 4. Financials
                item {
                     DetailSection(title = "FINANCEIRO") {
                        FinancialRow("Valor Total", reservation.totalRevenue, isTotal = true)
                        Divider(Modifier.padding(vertical = 4.dp))
                        FinancialRow("Taxa de Limpeza", reservation.cleaningFee)
                        FinancialRow("Receita Base", reservation.baseRevenue)
                        FinancialRow("Comissão", reservation.commissionAmount)
                        FinancialRow("Receita Líquida", reservation.netRevenue, isHighlight = true)
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Badge(reservation.paymentStatus ?: "Pendente", 
                                if (reservation.paymentStatus == "pago") MaterialTheme.colorScheme.tertiaryContainer 
                                else MaterialTheme.colorScheme.surfaceVariant)
                            Spacer(modifier = Modifier.width(8.dp))
                            if (reservation.paymentDate != null) {
                                Text("em ${DateUtils.formatIsoToBrazilian(reservation.paymentDate)}", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                     }
                }

                // 5. Cleaning
                item {
                    DetailSection(title = "LIMPEZA") {
                         Text("Faxineira: ${cleaner?.fullName ?: "Não atribuída"}", fontWeight = FontWeight.Bold)
                         Spacer(modifier = Modifier.height(4.dp))
                         Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Badge(reservation.cleaningStatus ?: "Pendente", MaterialTheme.colorScheme.primaryContainer)
                            Badge(reservation.cleaningPaymentStatus ?: "Pendente", MaterialTheme.colorScheme.secondaryContainer)
                         }
                         if (reservation.cleaningRating != null && reservation.cleaningRating > 0) {
                             Spacer(modifier = Modifier.height(4.dp))
                             Text("Avaliação: ${"⭐".repeat(reservation.cleaningRating)}", style = MaterialTheme.typography.bodySmall)
                         }
                         if (!reservation.cleaningNotes.isNullOrBlank()) {
                             Spacer(modifier = Modifier.height(4.dp))
                             Text("Notas: \"${reservation.cleaningNotes}\"", style = MaterialTheme.typography.bodySmall, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                         }
                    }
                }

                // 6. Communication
                item {
                    DetailSection(title = "COMUNICAÇÃO") {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                if (reservation.isCommunicated == true) Icons.Default.Check else Icons.Default.Close, 
                                contentDescription = null, 
                                tint = if (reservation.isCommunicated == true) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Hóspede comunicado")
                        }
                         Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                if (reservation.receiptSent == true) Icons.Default.Check else Icons.Default.Close, 
                                contentDescription = null, 
                                tint = if (reservation.receiptSent == true) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Recibo enviado")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailSection(title: String, content: @Composable () -> Unit) {
    Column {
        Text(title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(Modifier.padding(16.dp)) {
                content()
            }
        }
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

@Composable
fun FinancialRow(label: String, value: Double?, isTotal: Boolean = false, isHighlight: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label, 
            style = if (isTotal) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
            fontWeight = if (isTotal) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            CurrencyUtils.formatBRL(value ?: 0.0),
            style = if (isTotal) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
            fontWeight = if (isTotal || isHighlight) FontWeight.Bold else FontWeight.Normal,
            color = if (isHighlight) MaterialTheme.colorScheme.primary else Color.Unspecified
        )
    }
}


data class ReservationDetailUiState(
    val reservation: Reservation? = null,
    val property: com.riohhost.app.data.models.Property? = null,
    val cleaner: com.riohhost.app.data.models.UserProfile? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class ReservationDetailViewModel(
    private val repository: com.riohhost.app.data.repositories.ReservationRepository = com.riohhost.app.data.repositories.ReservationRepository(),
    private val propertyRepository: com.riohhost.app.data.repositories.PropertyRepository = com.riohhost.app.data.repositories.PropertyRepository(),
    private val userManagementRepository: com.riohhost.app.data.repositories.UserManagementRepository = com.riohhost.app.data.repositories.UserManagementRepository()
) : androidx.lifecycle.ViewModel() {
    private val _uiState = kotlinx.coroutines.flow.MutableStateFlow(ReservationDetailUiState())
    val uiState = _uiState.asStateFlow()
    
    // Maintain backward compatibility for existing observers if any, though we should migrate them in the UI
    @Deprecated("Use uiState.reservation instead")
    val reservation = _uiState.map { it.reservation }

    fun loadReservation(id: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val res = repository.getReservationById(id)
            
            if (res != null) {
                var property: com.riohhost.app.data.models.Property? = null
                var cleaner: com.riohhost.app.data.models.UserProfile? = null
                
                // Fetch related data in parallel
                val jobs = listOf(
                    launch {
                        if (res.propertyId != null) {
                            property = propertyRepository.getPropertyById(res.propertyId)
                        }
                    },
                    launch {
                        if (res.cleanerUserId != null) {
                            cleaner = userManagementRepository.getUserProfile(res.cleanerUserId)
                        }
                    }
                )
                jobs.forEach { it.join() }
                
                _uiState.value = ReservationDetailUiState(
                    reservation = res,
                    property = property,
                    cleaner = cleaner,
                    isLoading = false
                )
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Reserva não encontrada")
            }
        }
    }
}
