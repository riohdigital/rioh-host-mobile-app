package com.riohhost.app.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.riohhost.app.ui.GlobalFiltersViewModel
import com.riohhost.app.ui.components.KPICard
import com.riohhost.app.ui.theme.AirbnbColor
import com.riohhost.app.ui.theme.BookingColor
import com.riohhost.app.ui.theme.DirectColor
import com.riohhost.app.utils.CurrencyUtils

/**
 * Dashboard screen that reacts to GlobalFiltersViewModel changes.
 * Must receive the shared GlobalFiltersViewModel from NavGraph.
 */
@Composable
fun DashboardScreen(
    globalFiltersViewModel: GlobalFiltersViewModel
) {
    // Collect filter values
    val dateRangeStrings by globalFiltersViewModel.dateRangeStrings.collectAsState()
    val selectedProperties by globalFiltersViewModel.selectedProperties.collectAsState()
    val selectedPlatform by globalFiltersViewModel.selectedPlatform.collectAsState()
    
    // Create ViewModel that will load data based on filters
    val viewModel = androidx.lifecycle.viewmodel.compose.viewModel<DashboardViewModel>()
    val uiState by viewModel.uiState.collectAsState()
    
    // Load data whenever filters change
    LaunchedEffect(dateRangeStrings, selectedProperties, selectedPlatform) {
        android.util.Log.d("DashboardScreen", "Filtros mudaram: $dateRangeStrings, props: $selectedProperties, platform: $selectedPlatform")
        viewModel.loadDashboardData(
            startDate = dateRangeStrings.first,
            endDate = dateRangeStrings.second,
            propertyIds = if (selectedProperties.contains("todas")) null else selectedProperties,
            platform = if (selectedPlatform == "all") null else selectedPlatform
        )
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
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
            IconButton(onClick = {
                viewModel.loadDashboardData(
                    startDate = dateRangeStrings.first,
                    endDate = dateRangeStrings.second,
                    propertyIds = if (selectedProperties.contains("todas")) null else selectedProperties,
                    platform = if (selectedPlatform == "all") null else selectedPlatform
                )
            }) {
                Icon(Icons.Default.Refresh, contentDescription = "Atualizar")
            }
        }
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
                    // Row 1: Receita Total e Receita Líquida
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                            KPICard(title = "Receita Total", value = CurrencyUtils.formatBRL(state.kpis.totalRevenue), modifier = Modifier.weight(1f))
                            KPICard(title = "Receita Líquida", value = CurrencyUtils.formatBRL(state.kpis.netRevenue), modifier = Modifier.weight(1f))
                        }
                    }
                    
                    // Row 2: Despesas e Lucro
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                            KPICard(title = "Despesas", value = CurrencyUtils.formatBRL(state.kpis.totalExpenses), modifier = Modifier.weight(1f), valueColor = MaterialTheme.colorScheme.error)
                            KPICard(title = "Lucro Líquido", value = CurrencyUtils.formatBRL(state.kpis.netProfit), modifier = Modifier.weight(1f), valueColor = if (state.kpis.netProfit >= 0) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error)
                        }
                    }
                    
                    // Row 3: Ocupação e Reservas
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                            KPICard(title = "Taxa de Ocupação", value = String.format("%.1f%%", state.kpis.occupancyRate), modifier = Modifier.weight(1f))
                            KPICard(title = "Reservas", value = state.kpis.totalReservations.toString(), modifier = Modifier.weight(1f))
                        }
                    }
                    
                    // Row 4: Imóveis Ativos e Comissão
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                            KPICard(title = "Imóveis Ativos", value = state.kpis.activeProperties.toString(), modifier = Modifier.weight(1f))
                            KPICard(title = "Comissões", value = CurrencyUtils.formatBRL(state.kpis.totalCommission), modifier = Modifier.weight(1f))
                        }
                    }
                    
                    // Payment Status Section
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Status de Pagamentos", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(vertical = 8.dp))
                    }
                    
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                            PaymentStatusCard(
                                label = "Pago",
                                count = state.kpis.paymentStatusCounts.paid,
                                color = Color(0xFF4CAF50),
                                modifier = Modifier.weight(1f)
                            )
                            PaymentStatusCard(
                                label = "Pendente",
                                count = state.kpis.paymentStatusCounts.pending,
                                color = Color(0xFFFFA726),
                                modifier = Modifier.weight(1f)
                            )
                            PaymentStatusCard(
                                label = "Atrasado",
                                count = state.kpis.paymentStatusCounts.overdue,
                                color = Color(0xFFF44336),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    
                    // Revenue by Platform section
                    if (state.kpis.revenueByPlatform.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "Receita por Plataforma", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(vertical = 8.dp))
                        }
                        
                        item {
                            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    state.kpis.revenueByPlatform.forEach { (platform, revenue) ->
                                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(getPlatformColor(platform)))
                                                Spacer(modifier = Modifier.size(8.dp))
                                                Text(text = platform, style = MaterialTheme.typography.bodyMedium)
                                            }
                                            Text(text = CurrencyUtils.formatBRL(revenue), fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    // Extra padding at bottom
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }
    }
}

@Composable
private fun PaymentStatusCard(
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
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
