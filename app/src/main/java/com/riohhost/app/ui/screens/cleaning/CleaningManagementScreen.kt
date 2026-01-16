package com.riohhost.app.ui.screens.cleaning

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.riohhost.app.ui.screens.cleaning.components.CleaningActionsBottomSheet
import com.riohhost.app.ui.screens.cleaning.components.CleaningCardItem
import com.riohhost.app.ui.screens.cleaning.components.CleaningStatsScreen
import com.riohhost.app.ui.screens.cleaning.models.CleaningTab
import com.riohhost.app.data.models.ReservationWithCleanerInfo
import com.riohhost.app.ui.screens.cleaning.models.CleaningUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CleaningManagementScreen(
    onNavigateBack: () -> Unit,
    viewModel: CleaningManagementViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val showBottomSheet by viewModel.showBottomSheet.collectAsState()
    val selectedCleaning by viewModel.selectedCleaning.collectAsState()
    val permissions by viewModel.cleaningPermissions.collectAsState()
    val propertyCleaners by viewModel.propertyCleaners.collectAsState()

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
                    IconButton(onClick = { viewModel.onRefresh() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Atualizar")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            CleaningContent(
                uiState = uiState,
                onRefresh = viewModel::onRefresh,
                onCleaningClick = viewModel::onCleaningCardClick,
                onSearchQueryChanged = viewModel::onSearchQueryChanged,
                onTabSelected = viewModel::onTabSelected
            )
        }
    }

    if (showBottomSheet && selectedCleaning != null) {
        CleaningActionsBottomSheet(
            cleaning = selectedCleaning!!,
            permissions = permissions,
            propertyCleaners = propertyCleaners,
            onDismiss = viewModel::onDismissBottomSheet,
            onAssign = viewModel::assignCleanerToSelected,
            onUnassign = viewModel::unassignCleanerFromSelected,
            onReassign = viewModel::reassignCleanerForSelected,
            onToggleStatus = viewModel::toggleStatusForSelected
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CleaningContent(
    uiState: CleaningUiState,
    onRefresh: () -> Unit,
    onCleaningClick: (ReservationWithCleanerInfo) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onTabSelected: (CleaningTab) -> Unit
) {
    // Tab Items
    val tabs = listOf(
        "Todas" to CleaningTab.ALL,
        "Pendentes" to CleaningTab.PENDING,
        "Concluídas" to CleaningTab.COMPLETED,
        "Disponíveis" to CleaningTab.AVAILABLE,
        "Estatísticas" to CleaningTab.STATS
    )

    // Search Bar (Only visible for LIST tabs)
    if (uiState.selectedTab != CleaningTab.STATS) {
        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = { onSearchQueryChanged(it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            placeholder = { Text("Buscar por propriedade, hóspede...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            shape = MaterialTheme.shapes.medium,
            singleLine = true
        )
    }

    // Tabs
    ScrollableTabRow(
        selectedTabIndex = tabs.indexOfFirst { it.second == uiState.selectedTab },
        edgePadding = 16.dp,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        tabs.forEachIndexed { index, (title, tab) ->
            Tab(
                selected = uiState.selectedTab == tab,
                onClick = { onTabSelected(tab) },
                text = {
                    Text(
                        title,
                        style = if (uiState.selectedTab == tab) MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold) else MaterialTheme.typography.bodyMedium
                    )
                }
            )
        }
    }

    // Content
    Box(modifier = Modifier.fillMaxSize()) {
        if (uiState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else if (uiState.errorMessage != null) {
            Text(
                text = uiState.errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(16.dp),
                style = MaterialTheme.typography.bodyLarge
            )
        } else {
            if (uiState.selectedTab == CleaningTab.STATS) {
                CleaningStatsScreen(
                    cleanerStats = uiState.cleanerStats,
                    onFilterByCleaner = { cleanerName ->
                        // For now, we can filter filtering logic or just switch tabs.
                        // Let's just switch to ALL and filter by this cleaner if possible,
                        // but our ID/Name mapping is loose. For now, just a placeholder action.
                        // Ideally we map name back to ID or pass ID in stats.
                    }
                )
            } else {
                // Lists
                if (uiState.displayedCleanings.isEmpty()) {
                    Text(
                        text = "Nenhuma faxina encontrada.",
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.outline
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 80.dp) // Space for FAB if needed
                    ) {
                        items(uiState.displayedCleanings, key = { it.id }) { cleaning ->
                            CleaningCardItem(
                                cleaning = cleaning,
                                onClick = { onCleaningClick(cleaning) }
                            )
                        }
                    }
                }
            }
        }
    }
}
