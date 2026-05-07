// app/src/main/java/com/example/llmchatbot/data/local/MessageDao.kt
package com.example.c81.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {

    /** Reactive stream — the UI collects this and recomposes on every change. */
    @Query("SELECT * FROM messages WHERE username = :username ORDER BY timestamp ASC")
    fun observeMessages(username: String): Flow<List<MessageEntity>>

    /** One-shot snapshot — used to build conversation history for the LLM call. */
    @Query("SELECT * FROM messages WHERE username = :username ORDER BY timestamp ASC")
    suspend fun getMessagesForUser(username: String): List<MessageEntity>

    @Insert
    suspend fun insert(message: MessageEntity): Long

    @Query("DELETE FROM messages WHERE username = :username")
    suspend fun clearForUser(username: String)
}