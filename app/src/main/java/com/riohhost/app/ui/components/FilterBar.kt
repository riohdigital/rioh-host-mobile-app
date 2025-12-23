package com.riohhost.app.ui.components

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import com.riohhost.app.data.models.Property
import com.riohhost.app.ui.GlobalFiltersViewModel
import com.riohhost.app.utils.DateRangeCalculator

@Composable
fun FilterBar(
    filtersViewModel: GlobalFiltersViewModel,
    properties: List<Property> = emptyList(),
    modifier: Modifier = Modifier
) {
    val selectedPeriod by filtersViewModel.selectedPeriod.collectAsState()
    val selectedPlatform by filtersViewModel.selectedPlatform.collectAsState()
    val selectedProperties by filtersViewModel.selectedProperties.collectAsState()
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterDropdown(
                    label = "Período",
                    selectedValue = getPeriodLabel(selectedPeriod),
                    options = DateRangeCalculator.getAllPeriods().map { it.first to it.second },
                    onSelect = { filtersViewModel.setSelectedPeriod(it) },
                    modifier = Modifier.weight(1f),
                    icon = { Icon(Icons.Default.DateRange, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
                )
                
                FilterDropdown(
                    label = "Plataforma",
                    selectedValue = getPlatformLabel(selectedPlatform),
                    options = GlobalFiltersViewModel.PLATFORMS,
                    onSelect = { filtersViewModel.setSelectedPlatform(it) },
                    modifier = Modifier.weight(1f),
                    icon = { Icon(Icons.Default.Home, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
                )
            }
            
            if (properties.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                PropertyFilter(
                    properties = properties,
                    selectedIds = selectedProperties,
                    onSelectionChange = { filtersViewModel.setSelectedProperties(it) }
                )
            }
        }
    }
}

@Composable
private fun FilterDropdown(
    label: String,
    selectedValue: String,
    options: List<Pair<String, String>>,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    icon: @Composable (() -> Unit)? = null
) {
    var expanded by remember { mutableStateOf(false) }
    
    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surface)
                .clickable { expanded = true }
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                icon()
                Spacer(modifier = Modifier.width(8.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = selectedValue,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { (code, label) ->
                DropdownMenuItem(
                    text = { Text(label) },
                    onClick = {
                        onSelect(code)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun PropertyFilter(
    properties: List<Property>,
    selectedIds: List<String>,
    onSelectionChange: (List<String>) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    val displayText = when {
        selectedIds.contains("todas") -> "Todas as Propriedades"
        selectedIds.size == 1 -> properties.find { it.id == selectedIds.first() }?.nickname ?: "1 selecionada"
        else -> "${selectedIds.size} selecionadas"
    }
    
    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surface)
                .clickable { expanded = true }
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Home,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Propriedades",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = displayText,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Todas") },
                onClick = {
                    onSelectionChange(listOf("todas"))
                    expanded = false
                }
            )
            
            properties.forEach { property ->
                val isSelected = selectedIds.contains(property.id)
                DropdownMenuItem(
                    text = { 
                        Text(
                            text = "${if (isSelected) "✓ " else ""}${property.nickname ?: property.name}",
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        ) 
                    },
                    onClick = {
                        val newSelection = if (isSelected) {
                            selectedIds.filter { it != property.id }.ifEmpty { listOf("todas") }
                        } else {
                            (selectedIds.filter { it != "todas" } + property.id)
                        }
                        onSelectionChange(newSelection)
                    }
                )
            }
        }
    }
}

private fun getPeriodLabel(period: String): String {
    return DateRangeCalculator.getAllPeriods().find { it.first == period }?.second ?: "Ano Atual"
}

private fun getPlatformLabel(platform: String): String {
    return GlobalFiltersViewModel.PLATFORMS.find { it.first == platform }?.second ?: "Todas"
}
