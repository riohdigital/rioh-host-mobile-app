package com.riohhost.app.ui.screens.properties

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
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.riohhost.app.data.models.PropertyStatus
import com.riohhost.app.data.models.PropertyType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PropertyFormScreen(
    propertyId: String? = null,
    onNavigateBack: () -> Unit,
    viewModel: PropertyFormViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val formState by viewModel.formState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val isEditMode = propertyId != null

    LaunchedEffect(propertyId) {
        if (propertyId != null) {
            viewModel.loadProperty(propertyId)
        }
    }

    LaunchedEffect(uiState) {
        when (uiState) {
            is PropertyFormUiState.Success -> {
                snackbarHostState.showSnackbar(
                    if (isEditMode) "Propriedade atualizada!" else "Propriedade criada!"
                )
                onNavigateBack()
            }
            is PropertyFormUiState.Error -> {
                snackbarHostState.showSnackbar((uiState as PropertyFormUiState.Error).message)
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Editar Propriedade" else "Nova Propriedade") },
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
            is PropertyFormUiState.Loading -> {
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
                    // Name
                    OutlinedTextField(
                        value = formState.name,
                        onValueChange = { viewModel.updateName(it) },
                        label = { Text("Nome *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // Nickname
                    OutlinedTextField(
                        value = formState.nickname,
                        onValueChange = { viewModel.updateNickname(it) },
                        label = { Text("Apelido") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // Address
                    OutlinedTextField(
                        value = formState.address,
                        onValueChange = { viewModel.updateAddress(it) },
                        label = { Text("Endereço") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // Property Type and Status
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        Box(modifier = Modifier.weight(1f)) {
                            DropdownField(
                                label = "Tipo *",
                                value = formState.propertyType,
                                options = PropertyType.entries.map { it.displayName to it.displayName },
                                onSelect = { viewModel.updatePropertyType(it) }
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            DropdownField(
                                label = "Status *",
                                value = formState.status,
                                options = PropertyStatus.entries.map { it.displayName to it.displayName },
                                onSelect = { viewModel.updateStatus(it) }
                            )
                        }
                    }

                    // Links Row
                    OutlinedTextField(
                        value = formState.airbnbLink,
                        onValueChange = { viewModel.updateAirbnbLink(it) },
                        label = { Text("Link Airbnb") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = formState.bookingLink,
                        onValueChange = { viewModel.updateBookingLink(it) },
                        label = { Text("Link Booking") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // Financial Row
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = formState.commissionRate,
                            onValueChange = { viewModel.updateCommissionRate(it) },
                            label = { Text("Comissão %") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                        OutlinedTextField(
                            value = formState.cleaningFee,
                            onValueChange = { viewModel.updateCleaningFee(it) },
                            label = { Text("Taxa Limpeza R$") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                    }

                    // Price and Guests Row
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = formState.baseNightlyPrice,
                            onValueChange = { viewModel.updateBaseNightlyPrice(it) },
                            label = { Text("Diária R$") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                        OutlinedTextField(
                            value = formState.maxGuests,
                            onValueChange = { viewModel.updateMaxGuests(it) },
                            label = { Text("Max Hóspedes") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }

                    // Checkin/Checkout Times
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = formState.defaultCheckinTime,
                            onValueChange = { viewModel.updateDefaultCheckinTime(it) },
                            label = { Text("Check-in (HH:mm)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = formState.defaultCheckoutTime,
                            onValueChange = { viewModel.updateDefaultCheckoutTime(it) },
                            label = { Text("Check-out (HH:mm)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    // Notes
                    OutlinedTextField(
                        value = formState.notes,
                        onValueChange = { viewModel.updateNotes(it) },
                        label = { Text("Observações") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Save Button
                    Button(
                        onClick = {
                            if (isEditMode && propertyId != null) {
                                viewModel.updateProperty(propertyId)
                            } else {
                                viewModel.createProperty()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = formState.isValid && uiState !is PropertyFormUiState.Saving
                    ) {
                        if (uiState is PropertyFormUiState.Saving) {
                            CircularProgressIndicator(modifier = Modifier.padding(end = 8.dp))
                        }
                        Text(if (isEditMode) "Salvar Alterações" else "Criar Propriedade")
                    }
                }
            }
        }
    }
}

@Composable
private fun DropdownField(
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
