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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Handshake
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.riohhost.app.utils.CurrencyUtils

// Vibrant color palette matching the design reference
private val BlueGradient = listOf(Color(0xFF3B82F6), Color(0xFF1D4ED8))
private val TealGradient = listOf(Color(0xFF14B8A6), Color(0xFF0D9488))
private val OrangeGradient = listOf(Color(0xFFF59E0B), Color(0xFFD97706))
private val PurpleGradient = listOf(Color(0xFFA855F7), Color(0xFF7C3AED))

// Performance card colors
private val OccupancyBg = Color(0xFF1E293B)
private val ReservationsBg = Color(0xFF1E293B)
private val PropertiesBg = Color(0xFF1E293B)
private val CommissionBg = Color(0xFF1E293B)

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Header with refresh button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Dashboard",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            IconButton(onClick = { viewModel.refresh() }) {
                Icon(
                    Icons.Default.Refresh, 
                    contentDescription = "Atualizar",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(modifier = Modifier.height(20.dp))

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
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Main KPI Cards - Row 1
                    item {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            GradientKPICard(
                                title = "Receita Total",
                                value = CurrencyUtils.formatBRL(state.kpis.totalRevenue),
                                icon = Icons.Default.AttachMoney,
                                gradientColors = BlueGradient,
                                modifier = Modifier.weight(1f)
                            )
                            GradientKPICard(
                                title = "Receita Líquida",
                                value = CurrencyUtils.formatBRL(state.kpis.netRevenue),
                                icon = Icons.Outlined.Handshake,
                                gradientColors = TealGradient,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    
                    // Main KPI Cards - Row 2
                    item {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            GradientKPICard(
                                title = "Despesas",
                                value = CurrencyUtils.formatBRL(state.kpis.totalExpenses),
                                icon = Icons.Default.TrendingDown,
                                gradientColors = OrangeGradient,
                                modifier = Modifier.weight(1f)
                            )
                            GradientKPICard(
                                title = "Lucro Líquido",
                                value = CurrencyUtils.formatBRL(state.kpis.netProfit),
                                icon = Icons.Default.TrendingUp,
                                gradientColors = PurpleGradient,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    
                    // Performance Section Header
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Performance",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    
                    // Performance Cards Row 
                    item {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            PerformanceCard(
                                title = "Taxa de\nOcupação",
                                value = String.format("%.0f%%", state.kpis.occupancyRate),
                                subtitle = "Ocupado",
                                icon = Icons.Default.Percent,
                                iconColor = Color(0xFF22C55E),
                                modifier = Modifier.weight(1f)
                            )
                            PerformanceCard(
                                title = "Reservas",
                                value = state.kpis.totalReservations.toString(),
                                subtitle = "Ativas",
                                icon = Icons.Outlined.CalendarMonth,
                                iconColor = Color(0xFF3B82F6),
                                modifier = Modifier.weight(1f)
                            )
                            PerformanceCard(
                                title = "Imóveis\nAtivos",
                                value = state.kpis.activeProperties.toString(),
                                subtitle = "Gerenciados",
                                icon = Icons.Default.Home,
                                iconColor = Color(0xFF14B8A6),
                                modifier = Modifier.weight(1f)
                            )
                            PerformanceCard(
                                title = "Comissões",
                                value = CurrencyUtils.formatBRLCompact(state.kpis.totalCommission),
                                subtitle = "",
                                icon = Icons.Default.Percent,
                                iconColor = Color(0xFFF97316),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    
                    // Revenue by Platform section
                    if (state.kpis.revenueByPlatform.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Receita por Plataforma",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    state.kpis.revenueByPlatform.forEach { (platform, revenue) ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 6.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(12.dp)
                                                        .clip(RoundedCornerShape(3.dp))
                                                        .background(getPlatformColor(platform))
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = platform,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            Text(
                                                text = CurrencyUtils.formatBRL(revenue),
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    // Bottom spacing for FAB
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun GradientKPICard(
    title: String,
    value: String,
    icon: ImageVector,
    gradientColors: List<Color>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(140.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(colors = gradientColors)
                )
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Icon at top
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                // Title and Value at bottom
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = value,
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 22.sp),
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun PerformanceCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(110.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 14.sp,
                maxLines = 2
            )
            
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
            
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            if (subtitle.isNotBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun getPlatformColor(platform: String): Color {
    return when (platform.lowercase()) {
        "airbnb" -> Color(0xFFFF5A5F)
        "booking", "booking.com" -> Color(0xFF003580)
        "vrbo" -> Color(0xFF3D5A80)
        else -> Color(0xFF22C55E)
    }
}
