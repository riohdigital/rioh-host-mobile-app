package com.riohhost.app.ui.screens.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.riohhost.app.ui.GlobalFiltersViewModel
import com.riohhost.app.ui.components.FilterBar
import com.riohhost.app.ui.components.KPICard
import com.riohhost.app.ui.theme.AirbnbColor
import com.riohhost.app.ui.theme.BookingColor
import com.riohhost.app.ui.theme.DirectColor
import com.riohhost.app.utils.CurrencyUtils

@Composable
fun DashboardScreen(
    filtersViewModel: GlobalFiltersViewModel = viewModel(),
    dashboardViewModel: DashboardViewModel = viewModel()
) {
    val uiState by dashboardViewModel.uiState.collectAsState()
    val dateRange by filtersViewModel.dateRange.collectAsState()

    // Update dashboard when filters change
    androidx.compose.runtime.LaunchedEffect(dateRange, filtersViewModel.selectedProperties.collectAsState().value, filtersViewModel.selectedPlatform.collectAsState().value) {
        val (start, end) = filtersViewModel.dateRangeStrings.value
        dashboardViewModel.loadDashboardData(
            startDate = start,
            endDate = end,
            propertyIds = filtersViewModel.getPropertyFilter(),
            platform = filtersViewModel.getPlatformFilter()
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Dashboard",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
            IconButton(onClick = { dashboardViewModel.refresh() }) {
                Icon(Icons.Default.Refresh, contentDescription = "Atualizar")
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Filter Bar
        val properties = (uiState as? DashboardUiState.Success)?.properties ?: emptyList()
        FilterBar(
            filtersViewModel = filtersViewModel,
            properties = properties
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        when (val state = uiState) {
            is DashboardUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is DashboardUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = state.message, color = MaterialTheme.colorScheme.error)
                }
            }
            is DashboardUiState.Success -> {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                            KPICard(title = "Receita Total", value = CurrencyUtils.formatBRL(state.kpis.totalRevenue), modifier = Modifier.weight(1f))
                            KPICard(title = "Receita Líquida", value = CurrencyUtils.formatBRL(state.kpis.netRevenue), modifier = Modifier.weight(1f))
                        }
                    }
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                            KPICard(title = "Despesas", value = CurrencyUtils.formatBRL(state.kpis.totalExpenses), modifier = Modifier.weight(1f), valueColor = MaterialTheme.colorScheme.error)
                            KPICard(title = "Lucro Líquido", value = CurrencyUtils.formatBRL(state.kpis.netProfit), modifier = Modifier.weight(1f), valueColor = if (state.kpis.netProfit >= 0) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error)
                        }
                    }
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                            KPICard(title = "Taxa de Ocupação", value = String.format("%.1f%%", state.kpis.occupancyRate), modifier = Modifier.weight(1f))
                            KPICard(title = "Reservas", value = state.kpis.totalReservations.toString(), modifier = Modifier.weight(1f))
                        }
                    }
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                            KPICard(title = "Imóveis Ativos", value = state.kpis.activeProperties.toString(), modifier = Modifier.weight(1f))
                            KPICard(title = "Comissões", value = CurrencyUtils.formatBRL(state.kpis.totalCommission), modifier = Modifier.weight(1f))
                        }
                    }
                    
                    if (state.kpis.revenueByPlatform.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "Receita por Plataforma", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(vertical = 8.dp))
                        }
                        item {
                            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    state.kpis.revenueByPlatform.forEach { (platform, revenue) ->
                                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text(text = platform, color = getPlatformColor(platform))
                                            Text(text = CurrencyUtils.formatBRL(revenue), fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Propriedades (${state.properties.size})", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(vertical = 8.dp))
                    }

                    items(state.properties) { property ->
                        KPICard(title = property.nickname ?: property.name, value = property.status ?: "Indefinido", subtitle = property.address ?: "")
                    }
                }
            }
        }
    }
}

@Composable
private fun getPlatformColor(platform: String): Color {
    return when (platform.lowercase()) {
        "airbnb" -> AirbnbColor
        "booking", "booking.com" -> BookingColor
        else -> DirectColor
    }
}
