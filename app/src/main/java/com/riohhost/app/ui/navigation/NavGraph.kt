package com.riohhost.app.ui.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.riohhost.app.data.repositories.PropertyRepository
import com.riohhost.app.ui.GlobalFiltersViewModel
import com.riohhost.app.ui.components.GlobalFilterBar
import com.riohhost.app.ui.components.OwnerBottomNavigation
import com.riohhost.app.ui.screens.auth.LoginScreen
import com.riohhost.app.ui.screens.calendar.CalendarScreen
import com.riohhost.app.ui.screens.cleaner.CleanerDashboardScreen
import com.riohhost.app.ui.screens.dashboard.DashboardScreen
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
    object Properties : Screen("properties")
    object PropertyDetail : Screen("property_detail/{propertyId}") {
        fun createRoute(propertyId: String) = "property_detail/$propertyId"
    }
    object Chat : Screen("chat")
    object Users : Screen("users")
    object Alerts : Screen("alerts")
}

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Shared GlobalFiltersViewModel for all screens
    val globalFiltersViewModel: GlobalFiltersViewModel = viewModel()
    
    // Load properties for the filter bar
    val propertyRepository = remember { PropertyRepository() }
    val propertiesState = remember { kotlinx.coroutines.flow.MutableStateFlow<List<com.riohhost.app.data.models.Property>>(emptyList()) }
    
    androidx.compose.runtime.LaunchedEffect(Unit) {
        propertiesState.value = propertyRepository.getProperties()
    }
    
    val properties by propertiesState.collectAsState()

    val showBottomBar = currentRoute in listOf(
        Screen.Dashboard.route,
        Screen.Reservations.route,
        Screen.Calendar.route,
        Screen.Properties.route,
        Screen.Users.route
    )
    
    val showFilterBar = currentRoute in listOf(
        Screen.Dashboard.route,
        Screen.Reservations.route,
        Screen.Calendar.route
    )

    Scaffold(
        topBar = {
            if (showFilterBar) {
                GlobalFilterBar(
                    filtersViewModel = globalFiltersViewModel,
                    properties = properties
                )
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
                FloatingActionButton(
                    onClick = { navController.navigate(Screen.Chat.route) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Chat, contentDescription = "Chat AI")
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
                DashboardScreen(filtersViewModel = globalFiltersViewModel)
            }
            composable(Screen.Cleaner.route) {
                CleanerDashboardScreen()
            }
            composable(Screen.Reservations.route) {
                ReservationsListScreen(
                    filtersViewModel = globalFiltersViewModel,
                    onReservationClick = { reservationId ->
                        navController.navigate(Screen.ReservationDetail.createRoute(reservationId))
                    }
                )
            }
            composable(Screen.Calendar.route) {
                CalendarScreen(
                    filtersViewModel = globalFiltersViewModel,
                    onReservationClick = { reservationId ->
                        navController.navigate(Screen.ReservationDetail.createRoute(reservationId))
                    }
                )
            }
            composable(route = Screen.ReservationDetail.route) { backStackEntry ->
                val reservationId = backStackEntry.arguments?.getString("reservationId")
                com.riohhost.app.ui.screens.reservations.ReservationDetailScreen(
                    reservationId = reservationId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Properties.route) {
                com.riohhost.app.ui.screens.properties.PropertiesListScreen(
                    onPropertyClick = { propertyId ->
                        navController.navigate(Screen.PropertyDetail.createRoute(propertyId))
                    }
                )
            }
            composable(Screen.PropertyDetail.route) { backStackEntry ->
                val propertyId = backStackEntry.arguments?.getString("propertyId")
                com.riohhost.app.ui.screens.properties.PropertyDetailScreen(
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
        }
    }
}
