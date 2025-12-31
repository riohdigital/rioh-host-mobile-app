package com.riohhost.app.data.repositories

import com.riohhost.app.data.api.SupabaseClient
import com.riohhost.app.data.models.ChatMessage
import com.riohhost.app.data.models.MessageRole
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.datetime.Instant

private const val AI_CHAT_ENDPOINT = "https://n8n-n8n.dgyrua.easypanel.host/webhook/DashBoard%20RiohHost%20ChatAI"

@Serializable
data class ChatRequest(
    val message: String,
    val userId: String,
    val timestamp: String,
    val attachments: List<ChatAttachment>? = null
)

@Serializable
data class ChatAttachment(
    val name: String,
    val type: String,
    val size: Long,
    val data: String
)

@Serializable
data class ChatResponse(
    val response: String? = null,
    val message: String? = null
)

@Serializable
data class ChatHistoryRow(
    val id: Int,
    @SerialName("user_id") val userId: String,
    @SerialName("session_id") val sessionId: String,
    val message: ChatMessageContent,
    val category: String?,
    val reaction: String?,
    @SerialName("created_at") val createdAt: String,
    @SerialName("is_archived") val isArchived: Boolean
)

@Serializable
data class ChatMessageContent(
    val type: String,
    val content: String,
    @SerialName("additional_kwargs") val additionalKwargs: Map<String, String>? = null,
    @SerialName("response_metadata") val responseMetadata: Map<String, String>? = null
)

class AIChatRepository {
    private val supabase = SupabaseClient.client
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    suspend fun sendMessage(
        message: String,
        userId: String
    ): Result<String> {
        return try {
            val request = ChatRequest(
                message = message,
                userId = userId,
                timestamp = java.time.format.DateTimeFormatter.ISO_INSTANT.format(java.time.Instant.now()),
                attachments = null
            )

            val response = client.post(AI_CHAT_ENDPOINT) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }

            if (!response.status.isSuccess()) {
                throw Exception("Erro HTTP ${response.status.value}")
            }

            val data = response.body<ChatResponse>()
            val aiMessage = data.response ?: data.message 
                ?: "Desculpe, não consegui processar sua solicitação."
            
            Result.success(aiMessage)
        } catch (e: Exception) {
            println("AIChatRepo: Erro ao enviar mensagem: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun loadChatHistory(userId: String): Result<List<ChatMessage>> {
        return try {
            val data = supabase.postgrest.from("riohhost_chat_history")
                .select {
                    filter {
                        eq("user_id", userId)
                        eq("is_archived", false)
                    }
                    order("created_at", Order.ASCENDING)
                }
                .decodeList<ChatHistoryRow>()

            val messages = data.map { row ->
                ChatMessage(
                    id = row.id.toString(),
                    role = if (row.message.type == "human") MessageRole.USER else MessageRole.ASSISTANT,
                    content = row.message.content,
                    timestamp = row.createdAt
                )
            }
            Result.success(messages)
        } catch (e: Exception) {
            println("AIChatRepo: Erro ao carregar histórico: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun saveMessage(userId: String, message: ChatMessage): Result<Unit> {
        return try {
            val row = mapOf(
                "user_id" to userId,
                "session_id" to userId,
                "message" to mapOf(
                    "type" to if (message.role == MessageRole.USER) "human" else "ai",
                    "content" to message.content,
                    "additional_kwargs" to emptyMap<String, String>(),
                    "response_metadata" to emptyMap<String, String>()
                ),
                "category" to null,
                "created_at" to message.timestamp
            )
            
            supabase.postgrest.from("riohhost_chat_history")
                .insert(row)
            Result.success(Unit)
        } catch (e: Exception) {
            println("AIChatRepo: Erro ao salvar mensagem: ${e.message}")
            Result.failure(e)
        }
    }
    
    suspend fun clearHistory(userId: String): Result<Unit> {
        return try {
            supabase.postgrest.from("riohhost_chat_history")
                .delete {
                    filter { eq("user_id", userId) }
                }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
