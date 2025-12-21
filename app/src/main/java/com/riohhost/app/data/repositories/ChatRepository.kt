package com.riohhost.app.data.repositories

import com.riohhost.app.data.models.ChatMessage
import com.riohhost.app.data.models.MessageRole
import kotlinx.coroutines.delay
import java.util.UUID
import java.time.LocalDateTime

class ChatRepository {
    // In a real app, this would use Supabase Realtime or Edge Functions
    
    suspend fun sendMessage(content: String): ChatMessage {
        // Simulate network delay
        delay(1000)
        
        // Return a mock response
        return ChatMessage(
            id = UUID.randomUUID().toString(),
            role = MessageRole.ASSISTANT,
            content = "Esta é uma resposta simulada da IA para: '$content'. A integração real requer Edge Functions.",
            timestamp = LocalDateTime.now().toString()
        )
    }
}
