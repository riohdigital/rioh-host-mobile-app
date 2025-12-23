package com.riohhost.app.ui.screens.properties

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.riohhost.app.data.models.Property
import com.riohhost.app.data.repositories.PropertyRepository
import com.riohhost.app.utils.CurrencyUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PropertyDetailScreen(
    propertyId: String?,
    viewModel: PropertyDetailViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onNavigateBack: () -> Unit
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
                title = { Text(property?.nickname ?: "Detalhes do Imóvel") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
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
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Informações Básicas", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Nome Oficial: ${property!!.name}")
                        Text("Endereço: ${property!!.address ?: "N/A"}")
                        Text("Tipo: ${property!!.propertyType?.uppercase() ?: "N/A"}")
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Financeiro & Regras", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Diária Base")
                            Text(CurrencyUtils.formatBRL(property!!.baseNightlyPrice?.toDoubleOrNull() ?: 0.0), fontWeight = FontWeight.Bold)
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Taxa de Limpeza")
                            Text(CurrencyUtils.formatBRL(property!!.cleaningFee?.toDoubleOrNull() ?: 0.0), fontWeight = FontWeight.Bold)
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Capacidade")
                            Text("${property!!.maxGuests ?: 1} Hóspedes", fontWeight = FontWeight.Bold)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                if (!property!!.airbnbLink.isNullOrBlank() || !property!!.bookingLink.isNullOrBlank()) {
                    Text("Links Externos", style = MaterialTheme.typography.titleSmall)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (!property!!.airbnbLink.isNullOrBlank()) {
                            Button(onClick = { /* Open Link */ }, modifier = Modifier.weight(1f)) { Text("Airbnb") }
                        }
                        if (!property!!.bookingLink.isNullOrBlank()) {
                            Button(onClick = { /* Open Link */ }, modifier = Modifier.weight(1f)) { Text("Booking") }
                        }
                    }
                }
            }
        }
    }
}

class PropertyDetailViewModel(
    private val repository: PropertyRepository = PropertyRepository()
) : ViewModel() {
    private val _property = MutableStateFlow<Property?>(null)
    val property = _property.asStateFlow()

    fun loadProperty(id: String) {
        viewModelScope.launch {
            _property.value = repository.getPropertyById(id)
        }
    }
}
