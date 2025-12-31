package com.riohhost.app.ui.screens.properties

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.riohhost.app.data.models.PropertyFormData
import com.riohhost.app.data.models.PropertyStatus
import com.riohhost.app.data.models.PropertyType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PropertyFormScreen(
    propertyId: String? = null,
    viewModel: PropertyFormViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val existingProperty by viewModel.existingProperty.collectAsState()
    val context = LocalContext.current
    val isEdit = propertyId != null

    // Hardcoded User ID for now (Normally this comes from Auth Context)
    val userId = "current_user_id" // TODO: Get from AuthViewModel

    // Form State
    var name by remember { mutableStateOf("") }
    var nickname by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var cleaningFee by remember { mutableStateOf("") }
    var commissionRate by remember { mutableStateOf("20") }
    
    var selectedType by remember { mutableStateOf(PropertyType.APARTAMENTO.value) }
    var selectedStatus by remember { mutableStateOf(PropertyStatus.ATIVO.value) }

    LaunchedEffect(propertyId) {
        if (propertyId != null) {
            viewModel.loadProperty(propertyId)
        }
    }

    LaunchedEffect(existingProperty) {
        existingProperty?.let { p ->
            name = p.name
            nickname = p.nickname ?: ""
            address = p.address ?: ""
            cleaningFee = p.cleaningFee?.toString() ?: ""
            commissionRate = ((p.commissionRate ?: 0.2) * 100).toInt().toString()
            selectedType = p.propertyType ?: PropertyType.APARTAMENTO.value
            selectedStatus = p.status ?: PropertyStatus.ATIVO.value
        }
    }

    LaunchedEffect(uiState) {
        if (uiState is PropFormUiState.Success) {
            Toast.makeText(context, "Propriedade salva!", Toast.LENGTH_SHORT).show()
            onNavigateBack()
        }
        if (uiState is PropFormUiState.Error) {
            Toast.makeText(context, (uiState as PropFormUiState.Error).message, Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEdit) "Editar Propriedade" else "Nova Propriedade") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Voltar")
                    }
                }
            )
        }
    ) { padding ->
        if (uiState is PropFormUiState.Loading) {
             Column(
                modifier = Modifier.fillMaxSize().padding(padding), 
                verticalArrangement = Arrangement.Center, 
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nome da Propriedade") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = nickname,
                        onValueChange = { nickname = it },
                        label = { Text("Apelido (Opcional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                item {
                     // Reusing simple dropdown logic (should be in a shared component file ideally)
                     // Direct copy for now to ensure isolation
                     DropdownField("Tipo", PropertyType.values().map { it.value }, selectedType) { selectedType = it }
                }

                 item {
                     DropdownField("Status", PropertyStatus.values().map { it.value }, selectedStatus) { selectedStatus = it }
                }
                
                item {
                    OutlinedTextField(
                        value = cleaningFee,
                        onValueChange = { cleaningFee = it },
                        label = { Text("Taxa de Limpeza (R$)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }

                 item {
                    OutlinedTextField(
                        value = commissionRate,
                        onValueChange = { commissionRate = it },
                        label = { Text("Comissão (%) - Ex: 20") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
                
                item {
                    Button(
                        onClick = {
                            val data = PropertyFormData(
                                name = name,
                                nickname = nickname.ifBlank { null },
                                address = address.ifBlank { null },
                                property_type = selectedType,
                                status = selectedStatus,
                                cleaning_fee = cleaningFee,
                                commission_rate = commissionRate.toIntOrNull() ?: 20
                            )
                            viewModel.submitForm(isEdit, propertyId, data, userId)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = name.isNotBlank() && cleaningFee.isNotBlank()
                    ) {
                        Text("Salvar Propriedade")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownField(label: String, options: List<String>, selected: String, onSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }, modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            readOnly = true,
            value = selected,
            onValueChange = {},
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(text = { Text(option) }, onClick = { onSelected(option); expanded = false })
            }
        }
    }
}
