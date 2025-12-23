package com.riohhost.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.riohhost.app.ui.GlobalFiltersViewModel
import com.riohhost.app.ui.components.GlobalFilterBar
import com.riohhost.app.ui.components.OwnerBottomNavigation
import com.riohhost.app.ui.screens.auth.LoginScreen
import com.riohhost.app.ui.screens.calendar.CalendarScreen
import com.riohhost.app.ui.screens.cleaner.CleanerDashboardScreen
import com.riohhost.app.ui.screens.dashboard.DashboardScreen
import com.riohhost.app.ui.screens.properties.PropertyFormScreen
import com.riohhost.app.ui.screens.reservations.ReservationFormScreen
import com.riohhost.app.ui.screens.reservations.ReservationsListScreen

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Dashboard : Screen("dashboard")
    object Cleaner : Screen("cleaner")
    object Reservations : Screen("reservations")
    object Calendar : Screen("calendar")
    object ReservationDetail : Screen("reservation_detail/{reservationId}") {
        fun createRoute(reservationId: String) = "reservation_detail/$reservationId"
    }
    object ReservationForm : Screen("reservation_form")
    object ReservationEdit : Screen("reservation_edit/{reservationId}") {
        fun createRoute(reservationId: String) = "reservation_edit/$reservationId"
    }
    object Properties : Screen("properties")
    object PropertyDetail : Screen("property_detail/{propertyId}") {
        fun createRoute(propertyId: String) = "property_detail/$propertyId"
    }
    object PropertyForm : Screen("property_form")
    object PropertyEdit : Screen("property_edit/{propertyId}") {
        fun createRoute(propertyId: String) = "property_edit/$propertyId"
    }
    object Chat : Screen("chat")
    object Users : Screen("users")
    object Alerts : Screen("alerts")
    object CleaningManagement : Screen("cleaning_management")
}

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    // Global filters shared across screens
    val globalFiltersViewModel: GlobalFiltersViewModel = viewModel()

    val mainScreens = listOf(
        Screen.Dashboard.route,
        Screen.Reservations.route,
        Screen.Calendar.route,
        Screen.Properties.route,
        Screen.Users.route
    )
    val showBottomBar = currentRoute in mainScreens
    val showFilterBar = currentRoute in listOf(
        Screen.Dashboard.route,
        Screen.Reservations.route,
        Screen.Calendar.route
    )
    
    var fabExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            if (showFilterBar) {
                GlobalFilterBar(filtersViewModel = globalFiltersViewModel)
            }
        },
        bottomBar = {
            if (showBottomBar) {
                OwnerBottomNavigation(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(Screen.Dashboard.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            if (showBottomBar) {
                Column(horizontalAlignment = Alignment.End) {
                    // Expanded menu items
                    if (fabExpanded) {
                        // Chat AI
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Chat IA", style = MaterialTheme.typography.labelMedium)
                            Spacer(modifier = Modifier.width(8.dp))
                            SmallFloatingActionButton(
                                onClick = { 
                                    fabExpanded = false
                                    navController.navigate(Screen.Chat.route) 
                                },
                                containerColor = MaterialTheme.colorScheme.secondary
                            ) {
                                Icon(Icons.Default.Chat, contentDescription = "Chat AI", modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Cleaning Management
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Faxinas", style = MaterialTheme.typography.labelMedium)
                            Spacer(modifier = Modifier.width(8.dp))
                            SmallFloatingActionButton(
                                onClick = {
                                    fabExpanded = false
                                    navController.navigate(Screen.CleaningManagement.route)
                                },
                                containerColor = MaterialTheme.colorScheme.tertiary
                            ) {
                                Icon(Icons.Default.DateRange, contentDescription = "Faxinas", modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // New Reservation
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Nova Reserva", style = MaterialTheme.typography.labelMedium)
                            Spacer(modifier = Modifier.width(8.dp))
                            SmallFloatingActionButton(
                                onClick = {
                                    fabExpanded = false
                                    navController.navigate(Screen.ReservationForm.route)
                                },
                                containerColor = MaterialTheme.colorScheme.primary
                            ) {
                                Icon(Icons.Default.Person, contentDescription = "Nova Reserva", modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // New Property
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Novo Imóvel", style = MaterialTheme.typography.labelMedium)
                            Spacer(modifier = Modifier.width(8.dp))
                            SmallFloatingActionButton(
                                onClick = {
                                    fabExpanded = false
                                    navController.navigate(Screen.PropertyForm.route)
                                },
                                containerColor = MaterialTheme.colorScheme.primary
                            ) {
                                Icon(Icons.Default.Home, contentDescription = "Novo Imóvel", modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    
                    // Main FAB
                    FloatingActionButton(
                        onClick = { fabExpanded = !fabExpanded },
                        containerColor = if (fabExpanded) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    ) {
                        Icon(
                            if (fabExpanded) Icons.Default.Close else Icons.Default.Add,
                            contentDescription = if (fabExpanded) "Fechar" else "Ações"
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Login.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Login.route) {
                LoginScreen(
                    onNavigateToDashboard = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateToCleaner = {
                        navController.navigate(Screen.Cleaner.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Chat.route) {
                com.riohhost.app.ui.screens.chat.ChatScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Dashboard.route) {
                DashboardScreen()
            }
            composable(Screen.Cleaner.route) {
                CleanerDashboardScreen()
            }
            composable(Screen.Reservations.route) {
                ReservationsListScreen(
                    onReservationClick = { reservationId ->
                        navController.navigate(Screen.ReservationDetail.createRoute(reservationId))
                    },
                    onCreateNew = { navController.navigate(Screen.ReservationForm.route) }
                )
            }
            composable(Screen.Calendar.route) {
                CalendarScreen(
                    onReservationClick = { reservationId ->
                        navController.navigate(Screen.ReservationDetail.createRoute(reservationId))
                    }
                )
            }
            composable(Screen.ReservationDetail.route) { backStackEntry ->
                val reservationId = backStackEntry.arguments?.getString("reservationId")
                com.riohhost.app.ui.screens.reservations.ReservationDetailScreen(
                    reservationId = reservationId,
                    onNavigateBack = { navController.popBackStack() },
                    onEditClick = { id -> navController.navigate(Screen.ReservationEdit.createRoute(id)) }
                )
            }
            composable(Screen.ReservationForm.route) {
                ReservationFormScreen(
                    reservationId = null,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Screen.ReservationEdit.route) { backStackEntry ->
                val reservationId = backStackEntry.arguments?.getString("reservationId")
                ReservationFormScreen(
                    reservationId = reservationId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Properties.route) {
                com.riohhost.app.ui.screens.properties.PropertiesListScreen(
                    onPropertyClick = { propertyId ->
                        navController.navigate(Screen.PropertyDetail.createRoute(propertyId))
                    },
                    onCreateNew = { navController.navigate(Screen.PropertyForm.route) }
                )
            }
            composable(Screen.PropertyDetail.route) { backStackEntry ->
                val propertyId = backStackEntry.arguments?.getString("propertyId")
                com.riohhost.app.ui.screens.properties.PropertyDetailScreen(
                    propertyId = propertyId,
                    onNavigateBack = { navController.popBackStack() },
                    onEditClick = { id -> navController.navigate(Screen.PropertyEdit.createRoute(id)) }
                )
            }
            composable(Screen.PropertyForm.route) {
                PropertyFormScreen(
                    propertyId = null,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Screen.PropertyEdit.route) { backStackEntry ->
                val propertyId = backStackEntry.arguments?.getString("propertyId")
                PropertyFormScreen(
                    propertyId = propertyId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Users.route) {
                com.riohhost.app.ui.screens.usermanagement.UserListScreen()
            }
            composable(Screen.Alerts.route) {
                com.riohhost.app.ui.screens.alerts.AlertsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Screen.CleaningManagement.route) {
                com.riohhost.app.ui.screens.cleaning.CleaningManagementScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
