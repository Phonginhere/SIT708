// app/src/main/java/com/example/c81/ui/chat/components/MessageBubble.kt
package com.example.c81.ui.chat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.c81.data.local.MessageEntity
import com.example.c81.ui.theme.BotBubble
import com.example.c81.ui.theme.CyanTop
import com.example.c81.ui.theme.UserBubble
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MessageBubble(message: MessageEntity, username: String) {
    val isUser = message.isFromUser
    val bubbleColor = if (isUser) UserBubble else BotBubble

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        if (!isUser) {
            BotAvatar()
            Spacer(Modifier.width(8.dp))
        }

        Column(
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Surface(color = bubbleColor, shape = RoundedCornerShape(16.dp)) {
                Text(
                    text = message.content,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    color = Color.Black
                )
            }
            Text(
                text = formatTime(message.timestamp),
                color = Color.Gray,
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 2.dp, start = 6.dp, end = 6.dp)
            )
        }

        if (isUser) {
            Spacer(Modifier.width(8.dp))
            UserAvatar(username)
        }
    }
}

@Composable
fun BotAvatar() {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(CyanTop),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.SmartToy,
            contentDescription = "Bot",
            tint = Color.White,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun UserAvatar(username: String) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(CyanTop),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = username.firstOrNull()?.uppercaseChar()?.toString() ?: "U",
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun formatTime(timestamp: Long): String =
    SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))