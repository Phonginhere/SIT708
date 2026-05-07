// app/src/main/java/com/example/c81/ui/chat/ChatViewModel.kt
package com.example.c81.ui.chat

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.c81.data.local.MessageEntity
import com.example.c81.data.repository.ChatRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChatViewModel(
    private val username: String,
    private val repository: ChatRepository
) : ViewModel() {

    /** Reactive list of messages — UI collects this and recomposes. */
    val messages: StateFlow<List<MessageEntity>> =
        repository.observeMessages(username)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    var inputText by mutableStateOf("")
        private set

    var isSending by mutableStateOf(false)
        private set

    fun onInputChange(text: String) { inputText = text }

    fun onSend() {
        val text = inputText.trim()
        if (text.isEmpty() || isSending) return
        inputText = ""
        isSending = true
        viewModelScope.launch {
            try {
                repository.sendUserMessage(username, text)
            } finally {
                isSending = false
            }
        }
    }

    fun onClearChat() {
        viewModelScope.launch { repository.clearChat(username) }
    }

    /** Factory needed because the ViewModel takes constructor args. */
    class Factory(
        private val username: String,
        private val repository: ChatRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ChatViewModel(username, repository) as T
    }
}