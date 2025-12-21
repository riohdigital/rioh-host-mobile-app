package com.riohhost.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.riohhost.app.ui.screens.auth.LoginScreen
import com.riohhost.app.ui.screens.dashboard.DashboardScreen
import com.riohhost.app.ui.screens.cleaner.CleanerDashboardScreen
import androidx.compose.material.icons.filled.Chat

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import com.riohhost.app.ui.components.OwnerBottomNavigation
import com.riohhost.app.ui.screens.reservations.ReservationsListScreen
import com.riohhost.app.ui.screens.calendar.CalendarScreen

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

    val showBottomBar = currentRoute in listOf(
        Screen.Dashboard.route,
        Screen.Reservations.route,
        Screen.Calendar.route,
        Screen.Properties.route,
        Screen.Users.route
    )

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                OwnerBottomNavigation(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(Screen.Dashboard.route) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            if (showBottomBar) {
                androidx.compose.material3.FloatingActionButton(
                    onClick = { navController.navigate(Screen.Chat.route) },
                    containerColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                    contentColor = androidx.compose.ui.graphics.Color.White
                ) {
                    androidx.compose.material3.Icon(
                        androidx.compose.material.icons.Icons.Default.Chat,
                        contentDescription = "Chat AI"
                    )
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
                    }
                )
            }
            composable(Screen.Calendar.route) {
                CalendarScreen(
                    onReservationClick = { reservationId ->
                        navController.navigate(Screen.ReservationDetail.createRoute(reservationId))
                    }
                )
            }
            composable(
                route = Screen.ReservationDetail.route
            ) { backStackEntry ->
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
