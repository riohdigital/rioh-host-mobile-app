package com.riohhost.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.riohhost.app.data.models.Property
import com.riohhost.app.ui.GlobalFiltersViewModel
import com.riohhost.app.utils.DateRangeCalculator

@Composable
fun GlobalFilterBar(
    filtersViewModel: GlobalFiltersViewModel,
    properties: List<Property> = emptyList(),
    modifier: Modifier = Modifier
) {
    val selectedPeriod by filtersViewModel.selectedPeriod.collectAsState()
    val selectedPlatform by filtersViewModel.selectedPlatform.collectAsState()
    val selectedProperties by filtersViewModel.selectedProperties.collectAsState()
    var isExpanded by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
        shape = RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FilterChip(text = getPeriodLabel(selectedPeriod), onClick = { isExpanded = true })
                    Spacer(modifier = Modifier.width(8.dp))
                    if (selectedPlatform != "all") {
                        FilterChip(text = getPlatformLabel(selectedPlatform), onClick = { isExpanded = true })
                    }
                }
                IconButton(onClick = { isExpanded = !isExpanded }, modifier = Modifier.size(24.dp)) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Recolher" else "Expandir",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            AnimatedVisibility(visible = isExpanded, enter = expandVertically(), exit = shrinkVertically()) {
                Column(
                    modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CompactDropdown(
                            label = "Período",
                            selectedValue = getPeriodLabel(selectedPeriod),
                            options = DateRangeCalculator.getAllPeriods().map { it.first to it.second },
                            onSelect = { filtersViewModel.setSelectedPeriod(it) },
                            modifier = Modifier.weight(1f)
                        )
                        CompactDropdown(
                            label = "Plataforma",
                            selectedValue = getPlatformLabel(selectedPlatform),
                            options = GlobalFiltersViewModel.PLATFORMS,
                            onSelect = { filtersViewModel.setSelectedPlatform(it) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (properties.isNotEmpty()) {
                        CompactPropertyDropdown(
                            properties = properties,
                            selectedIds = selectedProperties,
                            onSelectionChange = { filtersViewModel.setSelectedProperties(it) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterChip(text: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun CompactDropdown(
    label: String,
    selectedValue: String,
    options: List<Pair<String, String>>,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        Surface(
            modifier = Modifier.fillMaxWidth().clickable { expanded = true },
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = selectedValue, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { (code, label) ->
                DropdownMenuItem(text = { Text(label, style = MaterialTheme.typography.bodySmall) }, onClick = { onSelect(code); expanded = false })
            }
        }
    }
}

@Composable
private fun CompactPropertyDropdown(
    properties: List<Property>,
    selectedIds: List<String>,
    onSelectionChange: (List<String>) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val displayText = when {
        selectedIds.contains("todas") -> "Todas (${properties.size})"
        selectedIds.size == 1 -> properties.find { it.id == selectedIds.first() }?.nickname ?: "1 selecionada"
        else -> "${selectedIds.size} selecionadas"
    }
    Box {
        Surface(
            modifier = Modifier.fillMaxWidth().clickable { expanded = true },
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Home, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Propriedades", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = displayText, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                }
                Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(text = { Text("Todas", style = MaterialTheme.typography.bodySmall) }, onClick = { onSelectionChange(listOf("todas")); expanded = false })
            properties.forEach { property ->
                val isSelected = selectedIds.contains(property.id)
                DropdownMenuItem(
                    text = { Text(text = "${if (isSelected) "✓ " else ""}${property.nickname ?: property.name}", style = MaterialTheme.typography.bodySmall, color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface) },
                    onClick = {
                        val newSelection = if (isSelected) selectedIds.filter { it != property.id }.ifEmpty { listOf("todas") } else (selectedIds.filter { it != "todas" } + property.id)
                        onSelectionChange(newSelection)
                    }
                )
            }
        }
    }
}

private fun getPeriodLabel(period: String): String = DateRangeCalculator.getAllPeriods().find { it.first == period }?.second ?: "Ano Atual"
private fun getPlatformLabel(platform: String): String = GlobalFiltersViewModel.PLATFORMS.find { it.first == platform }?.second ?: "Todas"
