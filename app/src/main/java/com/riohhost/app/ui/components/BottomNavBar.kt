package com.riohhost.app.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun OwnerBottomNavigation(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    NavigationBar {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
            label = { Text("Home") },
            selected = currentRoute == "dashboard",
            onClick = { onNavigate("dashboard") }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.List, contentDescription = "Reservas") },
            label = { Text("Reservas") },
            selected = currentRoute == "reservations",
            onClick = { onNavigate("reservations") }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.CalendarMonth, contentDescription = "Calendário") },
            label = { Text("Calendário") },
            selected = currentRoute == "calendar",
            onClick = { onNavigate("calendar") }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Imóveis") },
            label = { Text("Imóveis") },
            selected = currentRoute == "properties",
            onClick = { onNavigate("properties") }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Person, contentDescription = "Usuários") },
            label = { Text("Usuários") },
            selected = currentRoute == "users",
            onClick = { onNavigate("users") }
        )
    }
}
