package com.riohhost.app.ui.screens.reservations

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.riohhost.app.data.models.PaymentStatus
import com.riohhost.app.data.models.Platform
import com.riohhost.app.data.models.Property
import com.riohhost.app.data.models.ReservationStatus
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservationFormScreen(
    reservationId: String? = null,
    onNavigateBack: () -> Unit,
    viewModel: ReservationFormViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val formState by viewModel.formState.collectAsState()
    val properties by viewModel.properties.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val isEditMode = reservationId != null

    LaunchedEffect(reservationId) {
        if (reservationId != null) {
            viewModel.loadReservation(reservationId)
        }
        viewModel.loadProperties()
    }

    LaunchedEffect(uiState) {
        when (uiState) {
            is ReservationFormUiState.Success -> {
                snackbarHostState.showSnackbar(
                    if (isEditMode) "Reserva atualizada!" else "Reserva criada!"
                )
                onNavigateBack()
            }
            is ReservationFormUiState.Error -> {
                snackbarHostState.showSnackbar((uiState as ReservationFormUiState.Error).message)
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Editar Reserva" else "Nova Reserva") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        when (uiState) {
            is ReservationFormUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Property Dropdown
                    DropdownField(
                        label = "Propriedade *",
                        value = properties.find { it.id == formState.propertyId }?.nickname ?: properties.find { it.id == formState.propertyId }?.name ?: "Selecione",
                        options = properties.map { it.id to (it.nickname ?: it.name) },
                        onSelect = { viewModel.updatePropertyId(it) }
                    )

                    // Platform Dropdown
                    DropdownField(
                        label = "Plataforma *",
                        value = formState.platform,
                        options = Platform.entries.map { it.displayName to it.displayName },
                        onSelect = { viewModel.updatePlatform(it) }
                    )

                    // Reservation Code
                    OutlinedTextField(
                        value = formState.reservationCode,
                        onValueChange = { viewModel.updateReservationCode(it) },
                        label = { Text("Código da Reserva *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // Dates Row
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        DateField(
                            label = "Check-in *",
                            value = formState.checkInDate,
                            onValueChange = { viewModel.updateCheckInDate(it) },
                            modifier = Modifier.weight(1f)
                        )
                        DateField(
                            label = "Check-out *",
                            value = formState.checkOutDate,
                            onValueChange = { viewModel.updateCheckOutDate(it) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Guest Name
                    OutlinedTextField(
                        value = formState.guestName,
                        onValueChange = { viewModel.updateGuestName(it) },
                        label = { Text("Nome do Hóspede") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // Guest Phone & Email Row
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = formState.guestPhone,
                            onValueChange = { viewModel.updateGuestPhone(it) },
                            label = { Text("Telefone") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                        )
                        OutlinedTextField(
                            value = formState.numberOfGuests,
                            onValueChange = { viewModel.updateNumberOfGuests(it) },
                            label = { Text("Hóspedes") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }

                    // Total Revenue
                    OutlinedTextField(
                        value = formState.totalRevenue,
                        onValueChange = { viewModel.updateTotalRevenue(it) },
                        label = { Text("Valor Total (R$) *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )

                    // Status Dropdowns
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        Box(modifier = Modifier.weight(1f)) {
                            DropdownField(
                                label = "Status *",
                                value = formState.reservationStatus,
                                options = ReservationStatus.entries.map { it.displayName to it.displayName },
                                onSelect = { viewModel.updateReservationStatus(it) }
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            DropdownField(
                                label = "Pagamento",
                                value = formState.paymentStatus,
                                options = PaymentStatus.entries.map { it.displayName to it.displayName },
                                onSelect = { viewModel.updatePaymentStatus(it) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Save Button
                    Button(
                        onClick = {
                            if (isEditMode && reservationId != null) {
                                viewModel.updateReservation(reservationId)
                            } else {
                                viewModel.createReservation()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = formState.isValid && uiState !is ReservationFormUiState.Saving
                    ) {
                        if (uiState is ReservationFormUiState.Saving) {
                            CircularProgressIndicator(modifier = Modifier.padding(end = 8.dp))
                        }
                        Text(if (isEditMode) "Salvar Alterações" else "Criar Reserva")
                    }
                }
            }
        }
    }
}

@Composable
fun DropdownField(
    label: String,
    value: String,
    options: List<Pair<String, String>>,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    
    Box(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth().clickable { expanded = true },
            readOnly = true,
            trailingIcon = {
                Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.clickable { expanded = true })
            }
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { (key, display) ->
                DropdownMenuItem(
                    text = { Text(display) },
                    onClick = {
                        onSelect(key)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun DateField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    
    val date = try {
        if (value.isNotEmpty()) LocalDate.parse(value, formatter) else LocalDate.now()
    } catch (e: Exception) {
        LocalDate.now()
    }
    
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, day ->
            val selectedDate = LocalDate.of(year, month + 1, day)
            onValueChange(selectedDate.format(formatter))
        },
        date.year,
        date.monthValue - 1,
        date.dayOfMonth
    )
    
    OutlinedTextField(
        value = value,
        onValueChange = {},
        label = { Text(label) },
        modifier = modifier.clickable { datePickerDialog.show() },
        readOnly = true,
        trailingIcon = {
            Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.clickable { datePickerDialog.show() })
        }
    )
}
