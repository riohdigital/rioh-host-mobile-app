package com.riohhost.app.ui.screens.cleaning.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.riohhost.app.data.models.CleaningCleanerProfile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CleanerSelectorBottomSheet(
    cleaners: List<CleaningCleanerProfile>,
    onCleanerSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "Selecione uma Faxineira",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(16.dp)
            )
            Divider()
            LazyColumn {
                items(cleaners) { cleaner ->
                    ListItem(
                        headlineContent = { Text(cleaner.full_name ?: "Sem Nome") },
                        supportingContent = { Text(cleaner.phone ?: "") },
                        modifier = Modifier.clickable {
                            cleaner.user_id?.let { onCleanerSelected(it) }
                        }
                    )
                }
                if (cleaners.isEmpty()) {
                    item {
                        Text(
                            text = "Nenhuma faxineira disponível para esta propriedade.",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}