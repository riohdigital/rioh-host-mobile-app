package com.riohhost.app.ui.screens.cleaning.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssignmentInd
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.riohhost.app.data.models.CleaningCleanerProfile
import com.riohhost.app.data.models.ReservationWithCleanerInfo
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun CleaningCardItem(
    cleaning: ReservationWithCleanerInfo,
    cleaners: List<CleaningCleanerProfile>,
    onAssign: (String) -> Unit, // cleanerId
    onUnassign: () -> Unit,
    onToggleStatus: () -> Unit,
    canAssign: Boolean,
    canReassign: Boolean,
    canManage: Boolean
) {
    var showAssignDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Property Name + Status + Urgency
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = cleaning.properties?.name ?: "Propriedade Desconhecida",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "#${cleaning.reservation_code}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    // Status Badge
                    val statusColor = if (cleaning.cleaning_status == "Realizada") Color(0xFF4CAF50) else Color(0xFFFF9800)
                    val statusText = cleaning.cleaning_status ?: "Pendente"
                    
                    Surface(
                        color = statusColor.copy(alpha = 0.1f),
                        contentColor = statusColor,
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.padding(bottom = 4.dp)
                    ) {
                        Text(
                            text = statusText.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Urgency Badge
                    if (isUrgent(cleaning)) {
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.error,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)) {
                                Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "URGENTE",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Details Grid
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    DetailRow(Icons.Default.Event, "Check-out: ${formatDate(cleaning.check_out_date)}")
                    DetailRow(Icons.Default.Schedule, "Horário: ${cleaning.checkout_time ?: cleaning.properties?.default_checkin_time ?: "N/A"}")
                }
                Column(modifier = Modifier.weight(1f)) {
                    DetailRow(Icons.Default.Person, cleaning.guest_name ?: "Hóspede")
                    // Next Checkin if exists
                    if (cleaning.next_check_in_date != null) {
                        DetailRow(Icons.Default.Warning, "Próx. Check-in: ${formatDate(cleaning.next_check_in_date)}", isWarning = true)
                    }
                }
            }

            Divider(modifier = Modifier.padding(vertical = 12.dp))

            // Cleaner Assignment Section
            if (cleaning.cleaner_user_id != null && cleaning.cleaner_info != null) {
                // Cleaner Assigned
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Faxineira Atribuída:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = cleaning.cleaner_info.full_name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    Row {
                        if (canReassign) {
                            IconButton(onClick = onUnassign) {
                                Icon(Icons.Default.Delete, contentDescription = "Remover", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                        if (canManage) {
                            FilledTonalButton(
                                onClick = onToggleStatus,
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = if (cleaning.cleaning_status == "Realizada") MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primaryContainer
                                )
                            ) {
                                Text(if (cleaning.cleaning_status == "Realizada") "Reabrir" else "Concluir")
                            }
                        }
                    }
                }
            } else {
                // No Cleaner Assigned
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.AssignmentInd, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Sem faxineira atribuída",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.weight(1f)
                    )
                    
                    if (canAssign) {
                        Button(onClick = { showAssignDialog = true }) {
                            Text("Atribuir")
                        }
                    }
                }
            }
        }
    }

    if (showAssignDialog) {
        AssignCleanerDialog(
            cleaners = cleaners,
            onDismiss = { showAssignDialog = false },
            onAssign = { cleanerId ->
                onAssign(cleanerId)
                showAssignDialog = false
            }
        )
    }
}

@Composable
fun DetailRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String, isWarning: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
        Icon(
            imageVector = icon, 
            contentDescription = null, 
            modifier = Modifier.size(16.dp),
            tint = if (isWarning) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text, 
            style = MaterialTheme.typography.bodySmall,
            color = if (isWarning) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun AssignCleanerDialog(
    cleaners: List<CleaningCleanerProfile>,
    onDismiss: () -> Unit,
    onAssign: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Atribuir Faxineira") },
        text = {
            Column {
                cleaners.forEach { cleaner ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onAssign(cleaner.user_id) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(cleaner.full_name)
                    }
                    Divider()
                }
                if (cleaners.isEmpty()) {
                    Text("Nenhuma faxineira disponível para esta propriedade.", color = MaterialTheme.colorScheme.outline)
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

fun isUrgent(cleaning: ReservationWithCleanerInfo): Boolean {
    val checkoutDate = LocalDate.parse(cleaning.check_out_date) // Assuming ISO format from backend
    val tomorrow = LocalDate.now().plusDays(1)
    val isCompleted = cleaning.cleaning_status == "Realizada"
    val isFinalized = cleaning.reservation_status == "Finalizada"
    
    return checkoutDate <= tomorrow && !isCompleted && !isFinalized
}

fun formatDate(dateString: String): String {
    return try {
        val date = LocalDate.parse(dateString)
        date.format(DateTimeFormatter.ofPattern("dd/MM"))
    } catch (e: Exception) {
        dateString
    }
}
