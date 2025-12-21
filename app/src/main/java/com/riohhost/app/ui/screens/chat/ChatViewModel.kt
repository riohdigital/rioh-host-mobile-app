package com.riohhost.app.ui.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.riohhost.app.data.models.ChatMessage
import com.riohhost.app.data.models.MessageRole
import com.riohhost.app.data.repositories.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import java.time.LocalDateTime

class ChatViewModel(
    private val repository: ChatRepository = ChatRepository()
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun sendMessage(content: String) {
        if (content.isBlank()) return

        val userMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            role = MessageRole.USER,
            content = content,
            timestamp = LocalDateTime.now().toString(),
            status = "sending"
        )

        _messages.value = _messages.value + userMessage
        _isLoading.value = true

        viewModelScope.launch {
            try {
                val response = repository.sendMessage(content)
                _messages.value = _messages.value + response
            } catch (e: Exception) {
                // Handle error
                val errorMsg = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    role = MessageRole.SYSTEM,
                    content = "Erro ao enviar mensagem: ${e.message}",
                    timestamp = LocalDateTime.now().toString()
                )
                _messages.value = _messages.value + errorMsg
            } finally {
                _isLoading.value = false
            }
        }
    }
}
