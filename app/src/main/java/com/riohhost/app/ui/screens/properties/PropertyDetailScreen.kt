package com.riohhost.app.ui.screens.properties

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
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
import kotlinx.coroutines.flow.MutableStateFlow
import com.riohhost.app.data.models.Property
import com.riohhost.app.utils.CurrencyUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PropertyDetailScreen(
    propertyId: String?,
    viewModel: PropertyDetailViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onNavigateBack: () -> Unit,
    onEditClick: (String) -> Unit = {}
) {
    val property by viewModel.property.collectAsState()
    
    LaunchedEffect(propertyId) {
        if (propertyId != null) {
            viewModel.loadProperty(propertyId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(property?.nickname ?: property?.name ?: "Propriedade") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    if (propertyId != null) {
                        IconButton(onClick = { onEditClick(propertyId) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (propertyId != null) {
                ExtendedFloatingActionButton(
                    onClick = { onEditClick(propertyId) },
                    icon = { Icon(Icons.Default.Edit, contentDescription = null) },
                    text = { Text("Editar") },
                    containerColor = MaterialTheme.colorScheme.primary
                )
            }
        }
    ) { padding ->
        if (property == null) {
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
                // Header Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Home, contentDescription = null, modifier = Modifier.size(56.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = property!!.nickname ?: property!!.name,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            if (!property!!.address.isNullOrBlank()) {
                                Text(property!!.address!!, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                        // Status badge
                        val statusActive = property!!.status?.equals("Ativo", ignoreCase = true) == true
                        Surface(
                            color = if (statusActive) Color(0xFF4CAF50) else Color.Gray,
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                text = if (statusActive) "Ativo" else property!!.status ?: "Inativo",
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Info Cards
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Card(Modifier.weight(1f)) {
                        Column(Modifier.padding(16.dp)) {
                            Text("Tipo", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                            Text(property!!.propertyType ?: "N/A", fontWeight = FontWeight.Bold)
                        }
                    }
                    Card(Modifier.weight(1f)) {
                        Column(Modifier.padding(16.dp)) {
                            Text("Max Hóspedes", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                            Text("${property!!.maxGuests ?: "N/A"}", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Card(Modifier.weight(1f)) {
                        Column(Modifier.padding(16.dp)) {
                            Text("Check-in", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                            Text(property!!.defaultCheckinTime?.take(5) ?: "15:00", fontWeight = FontWeight.Bold)
                        }
                    }
                    Card(Modifier.weight(1f)) {
                        Column(Modifier.padding(16.dp)) {
                            Text("Check-out", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                            Text(property!!.defaultCheckoutTime?.take(5) ?: "11:00", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Financial Info
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Financeiro", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Diária Base")
                            Text(
                                CurrencyUtils.formatBRL(property!!.baseNightlyPrice ?: 0.0),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Taxa de Limpeza")
                            Text(CurrencyUtils.formatBRL(property!!.cleaningFee ?: 0.0))
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Comissão")
                            Text("${((property!!.commissionRate ?: 0.0) * 100).toInt()}%", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Links
                if (!property!!.airbnbLink.isNullOrBlank() || !property!!.bookingLink.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp)) {
                            Text("Links", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(8.dp))
                            if (!property!!.airbnbLink.isNullOrBlank()) {
                                Row {
                                    Surface(color = Color(0xFFFF5A5F), shape = MaterialTheme.shapes.small) {
                                        Text("Airbnb", color = Color.White, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Link configurado", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                }
                            }
                            if (!property!!.bookingLink.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Row {
                                    Surface(color = Color(0xFF003580), shape = MaterialTheme.shapes.small) {
                                        Text("Booking", color = Color.White, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Link configurado", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                }
                            }
                        }
                    }
                }

                if (!property!!.notes.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp)) {
                            Text("Observações", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(property!!.notes!!, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(80.dp)) // Space for FAB
            }
        }
    }
}

class PropertyDetailViewModel(
    private val repository: com.riohhost.app.data.repositories.PropertyRepository = com.riohhost.app.data.repositories.PropertyRepository()
) : androidx.lifecycle.ViewModel() {
    private val _property = MutableStateFlow<Property?>(null)
    val property = _property.asStateFlow()

    fun loadProperty(id: String) {
        viewModelScope.launch {
            _property.value = repository.getPropertyById(id)
        }
    }
}
