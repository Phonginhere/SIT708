// app/src/main/java/com/example/llmchatbot/data/repository/ChatRepository.kt
package com.example.c81.data.repository

import com.example.c81.data.local.MessageDao
import com.example.c81.data.local.MessageEntity
import com.example.c81.data.remote.GeminiClient
import kotlinx.coroutines.flow.Flow

/**
 * Single entry point for the ViewModel. Owns the orchestration of:
 *   user message persisted -> Gemini called with history -> reply persisted.
 * The UI never sees Gemini directly.
 */
class ChatRepository(
    private val dao: MessageDao,
    private val gemini: GeminiClient
) {
    fun observeMessages(username: String): Flow<List<MessageEntity>> =
        dao.observeMessages(username)

    suspend fun sendUserMessage(username: String, text: String) {
        // 1. persist the user's message immediately so the UI can render it
        dao.insert(
            MessageEntity(
                username = username,
                content = text,
                isFromUser = true,
                timestamp = System.currentTimeMillis()
            )
        )

        // 2. read prior turns (excluding the message we just inserted) for context
        val priorHistory = dao.getMessagesForUser(username).dropLast(1)

        // 3. call Gemini; on any failure, surface a friendly error as the bot reply
        val replyText = try {
            gemini.sendMessage(priorHistory, text)
        } catch (e: Exception) {
            "Error: ${e.localizedMessage ?: "could not reach Gemini"}"
        }

        // 4. persist the bot's reply — UI observes via Flow and re-renders
        dao.insert(
            MessageEntity(
                username = username,
                content = replyText,
                isFromUser = false,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun clearChat(username: String) = dao.clearForUser(username)
}