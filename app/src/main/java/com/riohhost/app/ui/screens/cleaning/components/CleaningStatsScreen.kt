package com.riohhost.app.ui.screens.cleaning.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.riohhost.app.data.models.CleanerStats

@Composable
fun CleaningStatsScreen(
    cleanerStats: Map<String, CleanerStats>,
    onFilterByCleaner: (String) -> Unit
) {
    if (cleanerStats.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
            Text("Sem dados estatísticos disponíveis.", color = MaterialTheme.colorScheme.outline)
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Desempenho por Faxineira",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            
            items(cleanerStats.values.toList()) { stats ->
                CleanerStatCard(stats = stats, onClick = { onFilterByCleaner(stats.name) }) // Ideally pass ID here, using name/key loosely for now
            }
        }
    }
}

@Composable
fun CleanerStatCard(stats: CleanerStats, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stats.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                StatItem(label = "Total", value = stats.total.toString())
                StatItem(label = "Pendentes", value = stats.pending.toString(), color = MaterialTheme.colorScheme.primary)
                StatItem(label = "Concluídas", value = stats.completed.toString(), color = Color(0xFF4CAF50))
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String, color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface) {
    Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = color)
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
    }
}
