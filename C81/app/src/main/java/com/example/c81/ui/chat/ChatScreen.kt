// app/src/main/java/com/example/c81/ui/chat/ChatScreen.kt
package com.example.c81.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.c81.data.repository.ChatRepository
import com.example.c81.ui.chat.components.BotAvatar
import com.example.c81.ui.chat.components.ChatInput
import com.example.c81.ui.chat.components.MessageBubble
import com.example.c81.ui.theme.BotBubble
import com.example.c81.ui.theme.CyanTop

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    username: String,
    repository: ChatRepository,
    onLogout: () -> Unit
) {
    val viewModel: ChatViewModel = viewModel(
        factory = ChatViewModel.Factory(username, repository)
    )
    val messages by viewModel.messages.collectAsState()
    val listState = rememberLazyListState()

    // Auto-scroll to bottom whenever a new message arrives or "typing" appears
    LaunchedEffect(messages.size, viewModel.isSending) {
        val lastIndex = messages.size - 1 + if (viewModel.isSending) 1 else 0
        if (lastIndex >= 0) {
            listState.animateScrollToItem(lastIndex)
        }
    }

    var showClearDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(username) },
                navigationIcon = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Logout")
                    }
                },
                actions = {
                    IconButton(onClick = { showClearDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Clear chat")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CyanTop,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.White)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
            ) {
                if (messages.isEmpty()) {
                    item { WelcomeBubble(username) }
                } else {
                    items(messages, key = { it.id }) { msg ->
                        MessageBubble(msg, username)
                    }
                }

                if (viewModel.isSending) {
                    item { TypingBubble() }
                }
            }

            ChatInput(
                value = viewModel.inputText,
                onValueChange = viewModel::onInputChange,
                onSend = viewModel::onSend,
                enabled = !viewModel.isSending
            )
        }
    }

    // Confirm before wiping the chat
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear chat?") },
            text = { Text("This will permanently delete this conversation.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.onClearChat()
                    showClearDialog = false
                }) { Text("Clear") }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun WelcomeBubble(username: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 6.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        BotAvatar()
        Spacer(Modifier.width(8.dp))
        Surface(color = BotBubble, shape = RoundedCornerShape(16.dp)) {
            Text(
                text = "Welcome $username!",
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                color = Color.Black
            )
        }
    }
}

@Composable
private fun TypingBubble() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        BotAvatar()
        Spacer(Modifier.width(8.dp))
        Surface(color = BotBubble, shape = RoundedCornerShape(16.dp)) {
            Text(
                text = "...",
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                color = Color.Gray
            )
        }
    }
}