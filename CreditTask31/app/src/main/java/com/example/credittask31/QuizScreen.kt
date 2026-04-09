package com.example.credittask31

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun QuizScreen(
    userName: String,
    isDarkMode: Boolean,
    onThemeToggle: (Boolean) -> Unit,
    onFinish: (score: Int, total: Int) -> Unit
) {
    val questions = remember { QuizData.getQuestions() }
    var currentIndex by remember { mutableIntStateOf(0) }
    var selectedIndex by remember { mutableIntStateOf(-1) }
    var hasSubmitted by remember { mutableStateOf(false) }
    var score by remember { mutableIntStateOf(0) }

    val question = questions[currentIndex]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        // Top bar: Welcome text + theme toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Welcome $userName!",
                fontSize = 18.sp,
                style = MaterialTheme.typography.titleMedium
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (isDarkMode) "☀️" else "🌙",
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.width(4.dp))
                Switch(
                    checked = isDarkMode,
                    onCheckedChange = onThemeToggle
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Subtask 2: Progress tracking
        Text(
            text = "${currentIndex + 1}/${questions.size}",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(4.dp))

        LinearProgressIndicator(
            progress = {
                if (hasSubmitted) (currentIndex + 1).toFloat() / questions.size
                else currentIndex.toFloat() / questions.size
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = question.title,
            fontSize = 20.sp,
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = question.detail,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Subtask 1: Answer options with visual feedback
        question.options.forEachIndexed { index, option ->
            val backgroundColor = when {
                !hasSubmitted && index == selectedIndex -> Color(0xFFBBDEFB)
                !hasSubmitted -> MaterialTheme.colorScheme.surfaceVariant
                index == question.correctIndex -> Color(0xFF4CAF50)
                index == selectedIndex -> Color(0xFFF44336)
                else -> MaterialTheme.colorScheme.surfaceVariant
            }

            val textColor = when {
                hasSubmitted && (index == question.correctIndex || index == selectedIndex) -> Color.White
                !hasSubmitted && index == selectedIndex -> Color(0xFF1A1A1A) // dark text on blue
                else -> MaterialTheme.colorScheme.onSurface
            }

            Button(
                onClick = {
                    if (!hasSubmitted) {
                        selectedIndex = index
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = backgroundColor,
                    contentColor = textColor,
                    disabledContainerColor = backgroundColor,
                    disabledContentColor = textColor
                ),
                enabled = !hasSubmitted
            ) {
                Text(option, fontSize = 15.sp)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Submit / Next button
        Button(
            onClick = {
                if (!hasSubmitted) {
                    if (selectedIndex == -1) return@Button
                    hasSubmitted = true
                    if (selectedIndex == question.correctIndex) {
                        score++
                    }
                } else {
                    if (currentIndex < questions.size - 1) {
                        currentIndex++
                        selectedIndex = -1
                        hasSubmitted = false
                    } else {
                        onFinish(score, questions.size)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            )
        ) {
            Text(
                text = when {
                    !hasSubmitted -> "Submit"
                    currentIndex < questions.size - 1 -> "Next"
                    else -> "Finish"
                },
                fontSize = 16.sp
            )
        }
    }
}