package com.riohhost.app.ui.screens.cleaning.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.riohhost.app.data.models.CleaningPermissions
import com.riohhost.app.data.models.ReservationWithCleanerInfo
import com.riohhost.app.data.models.CleaningCleanerProfile
import com.riohhost.app.ui.theme.RiohRed
import com.riohhost.app.ui.theme.RiohPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CleaningActionsBottomSheet(
    cleaning: ReservationWithCleanerInfo,
    permissions: CleaningPermissions,
    propertyCleaners: List<CleaningCleanerProfile>,
    onDismiss: () -> Unit,
    onAssign: (String) -> Unit,
    onUnassign: () -> Unit,
    onReassign: (String) -> Unit,
    onToggleStatus: () -> Unit
) {
    var showCleanerSelector by remember { mutableStateOf(false) }
    var selectorMode by remember { mutableStateOf<SelectorMode>(SelectorMode.Assign) }

    if (showCleanerSelector) {
        CleanerSelectorBottomSheet(
            cleaners = propertyCleaners,
            onCleanerSelected = { cleanerId ->
                showCleanerSelector = false
                if (selectorMode == SelectorMode.Assign) {
                    onAssign(cleanerId)
                } else {
                    onReassign(cleanerId)
                }
            },
            onDismiss = { showCleanerSelector = false }
        )
    } else {
        ModalBottomSheet(onDismissRequest = onDismiss) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .padding(bottom = 32.dp)
            ) {
                // Header
                Text(
                    text = cleaning.properties?.nickname ?: cleaning.properties?.name ?: "Propriedade sem nome",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Checkout: \${cleaning.check_out_date ?: "N/A"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Divider(modifier = Modifier.padding(vertical = 16.dp))

                val hasCleaner = cleaning.cleaner_user_id != null
                val isCompleted = cleaning.cleaning_status == "Realizada"

                // Cleaner Info Section
                if (hasCleaner) {
                    Text(
                        text = "FAXINEIRA ATRIBUÍDA",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    ListItem(
                        headlineContent = { Text(cleaning.cleaner_info?.full_name ?: "Desconhecido") },
                        supportingContent = { Text(cleaning.cleaner_info?.phone ?: "Sem telefone") },
                        leadingContent = { Icon(Icons.Default.Person, contentDescription = null) }
                    )
                } else {
                    Text(
                        text = "SEM FAXINEIRA ATRIBUÍDA",
                        style = MaterialTheme.typography.labelSmall,
                        color = RiohRed
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if (permissions.canAssign) {
                        Button(
                            onClick = { 
                                selectorMode = SelectorMode.Assign
                                showCleanerSelector = true 
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = RiohPrimary)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Atribuir Faxineira")
                        }
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 16.dp))
                Text("AÇÕES", style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(8.dp))

                // Actions
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    
                    // Reassign
                    if (hasCleaner && permissions.canReassign) {
                        OutlinedButton(
                            onClick = { 
                                selectorMode = SelectorMode.Reassign
                                showCleanerSelector = true 
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.SwapHoriz, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Trocar Faxineira")
                        }
                    }

                    // Unassign
                    if (hasCleaner && permissions.canReassign) {
                        OutlinedButton(
                            onClick = onUnassign,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = RiohRed)
                        ) {
                            Icon(Icons.Default.PersonRemove, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Remover Faxineira")
                        }
                    }

                    // Toggle Status
                    if (permissions.canManage) {
                        Button(
                            onClick = onToggleStatus,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isCompleted) Color.Gray else Color(0xFF4CAF50)
                            )
                        ) {
                            Icon(
                                if (isCompleted) Icons.Default.Undo else Icons.Default.CheckCircle, 
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (isCompleted) "Desmarcar Conclusão" else "Marcar como Concluída")
                        }
                    }
                }
            }
        }
    }
}

private enum class SelectorMode {
    Assign, Reassign
}