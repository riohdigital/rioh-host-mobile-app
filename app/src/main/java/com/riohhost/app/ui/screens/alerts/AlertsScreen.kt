package com.riohhost.app.ui.screens.alerts

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.riohhost.app.data.models.NotificationDestination
import com.riohhost.app.data.repositories.AlertsRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertsScreen(
    viewModel: AlertsViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    val destinations by viewModel.destinations.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Anfitrião Alerta") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { /* Add destination */ }) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    "Configure quem recebe alertas do sistema via WhatsApp.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
            items(destinations) { destination ->
                DestinationCard(destination)
            }
        }
    }
}

@Composable
fun DestinationCard(destination: NotificationDestination) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = destination.name,
                    style = MaterialTheme.typography.titleMedium
                )
                if (destination.isAuthenticated) {
                    Icon(Icons.Default.CheckCircle, contentDescription = "Autenticado", tint = Color.Green)
                } else {
                    Icon(Icons.Default.Warning, contentDescription = "Pendente", tint = Color.Yellow)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("WhatsApp: ${destination.whatsappNumber ?: "N/A"}")
            Text("Papel: ${destination.role}")
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
               Button(onClick = {}) { Text("Configurar") }
            }
        }
    }
}
