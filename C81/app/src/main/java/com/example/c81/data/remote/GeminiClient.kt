package com.example.c81.data.remote

import com.example.c81.data.local.MessageEntity
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content

class GeminiClient(apiKey: String) {
    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = apiKey
    )

    suspend fun sendMessage(history: List<MessageEntity>, userText: String): String {
        val chat = generativeModel.startChat(
            history = history.map {
                content(role = if (it.isFromUser) "user" else "model") { text(it.content) }
            }
        )
        val response = chat.sendMessage(userText)
        return response.text ?: "No response from Gemini"
    }
}