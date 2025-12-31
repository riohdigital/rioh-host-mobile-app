package com.riohhost.app.ui.screens.reservations

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.riohhost.app.data.models.CleaningPaymentStatus
import com.riohhost.app.data.models.PaymentStatus
import com.riohhost.app.data.models.Platform
import com.riohhost.app.data.models.ReservationFormData
import com.riohhost.app.data.models.ReservationStatus
import com.riohhost.app.utils.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservationFormScreen(
    reservationId: String? = null,
    viewModel: ReservationFormViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val existingReservation by viewModel.existingReservation.collectAsState()
    val context = LocalContext.current

    val isEdit = reservationId != null

    // Form State
    var reservationCode by remember { mutableStateOf("") }
    var guestName by remember { mutableStateOf("") }
    var checkInDate by remember { mutableStateOf("") }
    var checkOutDate by remember { mutableStateOf("") }
    var totalRevenue by remember { mutableStateOf("") }
    
    // Dropdowns
    var selectedPlatform by remember { mutableStateOf(Platform.AIRBNB.value) }
    var selectedStatus by remember { mutableStateOf(ReservationStatus.CONFIRMADA.value) }

    // Init Logic
    LaunchedEffect(reservationId) {
        if (reservationId != null) {
            viewModel.loadReservation(reservationId)
        }
    }

    LaunchedEffect(existingReservation) {
        existingReservation?.let { r ->
            reservationCode = r.reservationCode ?: ""
            guestName = r.guestName ?: ""
            checkInDate = r.checkInDate ?: ""
            checkOutDate = r.checkOutDate ?: ""
            totalRevenue = r.totalRevenue?.toString() ?: ""
            selectedPlatform = r.platform ?: Platform.AIRBNB.value
            selectedStatus = r.reservationStatus ?: ReservationStatus.CONFIRMADA.value
        }
    }

    LaunchedEffect(uiState) {
        if (uiState is FormUiState.Success) {
            Toast.makeText(context, "Reserva salva com sucesso!", Toast.LENGTH_SHORT).show()
            onNavigateBack()
        }
        if (uiState is FormUiState.Error) {
            Toast.makeText(context, (uiState as FormUiState.Error).message, Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEdit) "Editar Reserva" else "Nova Reserva") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Voltar")
                    }
                }
            )
        }
    ) { padding ->
        if (uiState is FormUiState.Loading) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding), 
                verticalArrangement = Arrangement.Center, 
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text("Informações Principais", style = MaterialTheme.typography.titleMedium)
                }

                item {
                    OutlinedTextField(
                        value = reservationCode,
                        onValueChange = { reservationCode = it },
                        label = { Text("Código da Reserva") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = guestName,
                        onValueChange = { guestName = it },
                        label = { Text("Nome do Hóspede") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = checkInDate,
                            onValueChange = { checkInDate = it }, // TODO: Add DatePicker
                            label = { Text("Check-in (yyyy-MM-dd)") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = checkOutDate,
                            onValueChange = { checkOutDate = it }, // TODO: Add DatePicker
                            label = { Text("Check-out (yyyy-MM-dd)") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Platform Selection
                item {
                    SimpleDropdown(
                        label = "Plataforma",
                        options = Platform.values().map { it.value },
                        selected = selectedPlatform,
                        onOptionSelected = { selectedPlatform = it }
                    )
                }

                item {
                    OutlinedTextField(
                        value = totalRevenue,
                        onValueChange = { totalRevenue = it },
                        label = { Text("Valor Total (R$)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }

                item {
                     SimpleDropdown(
                        label = "Status",
                        options = ReservationStatus.values().map { it.value },
                        selected = selectedStatus,
                        onOptionSelected = { selectedStatus = it }
                    )
                }
                
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            val data = ReservationFormData(
                                platform = selectedPlatform,
                                reservation_code = reservationCode,
                                check_in_date = checkInDate,
                                check_out_date = checkOutDate,
                                total_revenue = totalRevenue,
                                reservation_status = selectedStatus,
                                guest_name = guestName
                                // Add other fields as needed
                            )
                            viewModel.submitForm(isEdit, reservationId, data)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = reservationCode.isNotBlank() && checkInDate.isNotBlank()
                    ) {
                        Text("Salvar Reserva")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleDropdown(
    label: String,
    options: List<String>,
    selected: String,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            readOnly = true,
            value = selected,
            onValueChange = {},
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            options.forEach { selectionOption ->
                DropdownMenuItem(
                    text = { Text(selectionOption) },
                    onClick = {
                        onOptionSelected(selectionOption)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
        }
    }
}
