// app/src/main/java/com/example/c81/ChatBotApplication.kt
package com.example.c81

import android.app.Application
import com.example.c81.data.remote.GeminiClient
import com.example.c81.data.local.AppDatabase
import com.example.c81.data.repository.ChatRepository

/**
 * Manual DI container — singletons exposed as lazy properties.
 * ViewModels reach this via (application as ChatBotApplication).chatRepository.
 */
class ChatBotApplication : Application() {

    private val database by lazy { AppDatabase.get(this) }
    private val geminiClient by lazy { GeminiClient(BuildConfig.GEMINI_API_KEY) }

    val chatRepository by lazy { ChatRepository(database.messageDao(), geminiClient) }
}