// app/src/main/java/com/example/llmchatbot/data/local/MessageEntity.kt
package com.example.c81.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * One row per chat message. Scoped per user via [username]
 * so different logins keep independent histories.
 */
@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val username: String,
    val content: String,
    val isFromUser: Boolean,   // true = sent by user, false = Gemini reply
    val timestamp: Long        // System.currentTimeMillis() at creation
)