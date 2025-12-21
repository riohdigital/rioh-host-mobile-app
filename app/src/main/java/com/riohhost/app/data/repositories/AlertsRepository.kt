package com.riohhost.app.data.repositories

import com.riohhost.app.data.models.NotificationDestination
import kotlinx.coroutines.delay

class AlertsRepository {
    // Mock implementation for now as Supabase setup for this specific table is unknown
    suspend fun getDestinations(): List<NotificationDestination> {
        delay(500)
        return listOf(
            NotificationDestination(
                id = "1",
                name = "Meu WhatsApp",
                whatsappNumber = "+5521999999999",
                role = "Proprietário",
                isAuthenticated = true
            ),
            NotificationDestination(
                id = "2",
                name = "Gerente",
                whatsappNumber = "+5521988888888",
                role = "Gerente",
                isAuthenticated = false
            )
        )
    }
}
