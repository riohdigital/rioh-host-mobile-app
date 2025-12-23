package com.riohhost.app.ui.screens.cleaning

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.riohhost.app.data.models.CleanerProfile
import com.riohhost.app.data.models.CleaningReservation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CleaningManagementScreen(
    onNavigateBack: () -> Unit,
    viewModel: CleaningManagementViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val allCleanings by viewModel.allCleanings.collectAsState()
    val availableCleanings by viewModel.availableCleanings.collectAsState()
    val cleaners by viewModel.cleaners.collectAsState()
    val stats by viewModel.stats.collectAsState()
    
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Todas", "Pendentes", "Realizadas", "Disponíveis")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestão de Faxinas") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadData() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Atualizar")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Stats Cards
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard("Total", stats.totalCleanings, Color(0xFF2196F3), Modifier.weight(1f))
                StatCard("Pendentes", stats.pendingCleanings, Color(0xFFFFA726), Modifier.weight(1f))
                StatCard("Realizadas", stats.completedCleanings, Color(0xFF4CAF50), Modifier.weight(1f))
                StatCard("Disponíveis", stats.availableCleanings, Color(0xFF9C27B0), Modifier.weight(1f))
            }

            // Tabs
            ScrollableTabRow(selectedTabIndex = selectedTabIndex, edgePadding = 16.dp) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) }
                    )
                }
            }

            // Content
            when (uiState) {
                is CleaningUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is CleaningUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = (uiState as CleaningUiState.Error).message,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                is CleaningUiState.Success -> {
                    val cleaningsToShow = when (selectedTabIndex) {
                        0 -> allCleanings
                        1 -> viewModel.pendingCleanings
                        2 -> viewModel.completedCleanings
                        3 -> availableCleanings
                        else -> allCleanings
                    }

                    if (cleaningsToShow.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Nenhuma faxina encontrada", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize().padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(cleaningsToShow) { cleaning ->
                                CleaningCard(
                                    cleaning = cleaning,
                                    cleaners = cleaners,
                                    isAvailable = selectedTabIndex == 3,
                                    onAssign = { cleanerId -> viewModel.assignCleaning(cleaning.id, cleanerId) },
                                    onUnassign = { viewModel.unassignCleaning(cleaning.id) },
                                    onToggleStatus = { viewModel.toggleCleaningStatus(cleaning.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    label: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.15f))
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CleaningCard(
    cleaning: CleaningReservation,
    cleaners: List<CleanerProfile>,
    isAvailable: Boolean,
    onAssign: (String) -> Unit,
    onUnassign: () -> Unit,
    onToggleStatus: () -> Unit
) {
    var showCleanerDropdown by remember { mutableStateOf(false) }
    val isPending = cleaning.cleaningStatus?.equals("Pendente", ignoreCase = true) == true
    val isCompleted = cleaning.cleaningStatus?.equals("Realizada", ignoreCase = true) == true

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isCompleted -> Color(0xFF4CAF50).copy(alpha = 0.1f)
                isPending -> Color(0xFFFFA726).copy(alpha = 0.1f)
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Property name and status badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = cleaning.properties?.name ?: "Propriedade",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                StatusBadge(status = cleaning.cleaningStatus ?: "Indefinido")
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Dates
            Text(
                text = "Check-out: ${cleaning.checkOutDate ?: "N/A"} às ${cleaning.checkoutTime ?: "--:--"}",
                style = MaterialTheme.typography.bodyMedium
            )
            if (cleaning.nextCheckInDate != null) {
                Text(
                    text = "Próximo check-in: ${cleaning.nextCheckInDate} às ${cleaning.nextCheckinTime ?: "--:--"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            // Cleaner info or assign button
            Spacer(modifier = Modifier.height(12.dp))
            
            if (cleaning.cleanerInfo != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = cleaning.cleanerInfo.fullName, style = MaterialTheme.typography.bodyMedium)
                }
            }
            
            // Action buttons
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isAvailable || cleaning.cleanerUserId == null) {
                    // Assign button
                    Box {
                        Button(
                            onClick = { showCleanerDropdown = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Atribuir")
                        }
                        DropdownMenu(
                            expanded = showCleanerDropdown,
                            onDismissRequest = { showCleanerDropdown = false }
                        ) {
                            cleaners.forEach { cleaner ->
                                DropdownMenuItem(
                                    text = { Text(cleaner.fullName) },
                                    onClick = {
                                        onAssign(cleaner.userId)
                                        showCleanerDropdown = false
                                    }
                                )
                            }
                        }
                    }
                } else {
                    // Toggle status button
                    Button(
                        onClick = onToggleStatus,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isCompleted) Color(0xFFFFA726) else Color(0xFF4CAF50)
                        )
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isCompleted) "Marcar Pendente" else "Marcar Realizada")
                    }
                    
                    // Unassign button
                    Button(
                        onClick = onUnassign,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336))
                    ) {
                        Text("Remover")
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(status: String) {
    val (bgColor, textColor) = when (status.lowercase()) {
        "realizada" -> Color(0xFF4CAF50) to Color.White
        "pendente" -> Color(0xFFFFA726) to Color.White
        else -> Color.Gray to Color.White
    }
    
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = status,
            style = MaterialTheme.typography.labelSmall,
            color = textColor
        )
    }
}
