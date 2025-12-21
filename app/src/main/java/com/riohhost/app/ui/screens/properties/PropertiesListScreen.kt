package com.riohhost.app.ui.screens.properties

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.riohhost.app.data.models.Property
import com.riohhost.app.data.repositories.PropertyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@Composable
fun PropertiesListScreen(
    viewModel: PropertiesViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onPropertyClick: (String) -> Unit
) {
    val properties by viewModel.properties.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { /* TODO: Add Property */ }) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar Imóvel")
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
                text = "Meus Imóveis",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(properties) { property ->
                    PropertyItemCard(property, onClick = { onPropertyClick(property.id) })
                }
            }
        }
    }
}

@Composable
fun PropertyItemCard(property: Property, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Home, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(property.nickname ?: property.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                if (!property.address.isNullOrBlank()) {
                    Text(property.address, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
                Text(
                    text = if (property.status == "active") "Ativo" else "Inativo",
                    color = if (property.status == "active") Color(0xFF4CAF50) else Color.Gray,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

class PropertiesViewModel(
    private val repository: PropertyRepository = PropertyRepository()
) : ViewModel() {
    private val _properties = MutableStateFlow<List<Property>>(emptyList())
    val properties = _properties.asStateFlow()

    init {
        loadProperties()
    }

    private fun loadProperties() {
        viewModelScope.launch {
            _properties.value = repository.getProperties()
        }
    }
}
