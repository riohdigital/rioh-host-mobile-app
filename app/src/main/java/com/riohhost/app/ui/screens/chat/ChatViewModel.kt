package com.riohhost.app.ui.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.riohhost.app.data.models.ChatMessage
import com.riohhost.app.data.models.MessageRole
import com.riohhost.app.data.repositories.AIChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import java.time.Instant

class ChatViewModel : ViewModel() {
    private val repository = AIChatRepository()
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Mock User ID - should come from Auth
    private val userId = "current_user_id"

    init {
        loadHistory()
    }

    private fun loadHistory() {
        viewModelScope.launch {
            val result = repository.loadChatHistory(userId)
            result.onSuccess {
                _messages.value = it
            }
        }
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return

        val userMsg = ChatMessage(
            id = java.util.UUID.randomUUID().toString(),
            role = MessageRole.USER,
            content = text,
            timestamp = DateTimeFormatter.ISO_INSTANT.format(Instant.now())
        )

        // Optimistic update
        _messages.value = _messages.value + userMsg
        _isLoading.value = true

        viewModelScope.launch {
            // Save user message to Supabase
            repository.saveMessage(userId, userMsg)

            // Send to N8N
            val result = repository.sendMessage(text, userId)
            
            _isLoading.value = false
            
            result.onSuccess { aiResponseText ->
                val aiMsg = ChatMessage(
                    id = java.util.UUID.randomUUID().toString(),
                    role = MessageRole.ASSISTANT,
                    content = aiResponseText,
                    timestamp = DateTimeFormatter.ISO_INSTANT.format(Instant.now())
                )
                _messages.value = _messages.value + aiMsg
                // Save AI message to Supabase
                repository.saveMessage(userId, aiMsg)
            }.onFailure {
                // Handle error (maybe show toast or error message in chat)
                val errorMsg = ChatMessage(
                    id = java.util.UUID.randomUUID().toString(),
                    role = MessageRole.SYSTEM,
                    content = "Erro ao processar mensagem: ${it.message}",
                    timestamp = DateTimeFormatter.ISO_INSTANT.format(Instant.now())
                )
                _messages.value = _messages.value + errorMsg
            }
        }
    }
}
