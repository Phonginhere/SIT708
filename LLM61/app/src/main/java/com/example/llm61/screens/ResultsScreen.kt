package com.example.llm61.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.llm61.data.UserAnswer
import com.example.llm61.network.LlmUiState
import com.example.llm61.viewmodel.ResultsViewModel
import com.example.llm61.viewmodel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultsScreen(
    userViewModel: UserViewModel,
    onContinueClick: () -> Unit
) {
    val resultsViewModel: ResultsViewModel = viewModel()

    val answers = userViewModel.lastQuizAnswers
    val topic = userViewModel.lastQuizTopic
    val score = answers.count { it.isCorrect }
    val total = answers.size

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "✨ Answered by AI",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "Your Results",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            )
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Button(
                    onClick = onContinueClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text("Continue")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Score summary card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Topic: $topic",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "$score / $total",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Correct Answers",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // One card per answered question — each with its own AI explanation button
            answers.forEachIndexed { index, answer ->
                AnswerResultCard(
                    questionNumber = index + 1,
                    answer = answer,
                    explanationState = resultsViewModel.explanationStates[answer.question.id]
                        ?: LlmUiState.Idle,
                    onRequestExplanation = { resultsViewModel.requestExplanation(answer) },
                    onClearExplanation = { resultsViewModel.clearExplanation(answer.question.id) }
                )
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun AnswerResultCard(
    questionNumber: Int,
    answer: UserAnswer,
    explanationState: LlmUiState,
    onRequestExplanation: () -> Unit,
    onClearExplanation: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()       // smoothly resize when explanation expands
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header with right/wrong icon
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (answer.isCorrect) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Correct",
                        tint = Color(0xFF2E7D32)
                    )
                } else {
                    Icon(
                        Icons.Default.Cancel,
                        contentDescription = "Incorrect",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    "Question $questionNumber",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(8.dp))

            Text(
                answer.question.text,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(Modifier.height(8.dp))

            val userChoiceText = answer.selectedIndex
                ?.let { answer.question.options[it] }
                ?: "(no answer)"
            Text(
                "Your answer: $userChoiceText",
                style = MaterialTheme.typography.bodySmall,
                color = if (answer.isCorrect) Color(0xFF2E7D32)
                else MaterialTheme.colorScheme.error
            )

            if (!answer.isCorrect) {
                Text(
                    "Correct answer: ${answer.question.options[answer.question.correctAnswerIndex]}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF2E7D32)
                )
            }

            Spacer(Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(Modifier.height(8.dp))

            // ============= NEW: AI Explanation Section =============
            ExplanationSection(
                state = explanationState,
                onRequestExplanation = onRequestExplanation,
                onClearExplanation = onClearExplanation
            )
        }
    }
}

@Composable
private fun ExplanationSection(
    state: LlmUiState,
    onRequestExplanation: () -> Unit,
    onClearExplanation: () -> Unit
) {
    when (state) {
        LlmUiState.Idle -> {
            OutlinedButton(
                onClick = onRequestExplanation,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Default.AutoAwesome,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text("Explain with AI")
            }
        }

        LlmUiState.Loading -> {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    "Generating explanation...",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        is LlmUiState.Success -> {
            AnimatedVisibility(visible = true) {
                Column {
                    // Reusing the component from QuizScreen.kt
                    PromptAndResponseDisplay(
                        prompt = state.prompt,
                        response = state.response,
                        responseLabel = "🤖 AI Explanation",
                        responseColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                    TextButton(onClick = onClearExplanation) {
                        Text("Hide explanation")
                    }
                }
            }
        }

        is LlmUiState.Error -> {
            Column {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "❌ Couldn't get explanation",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            state.message,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onRequestExplanation,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Try Again")
                }
            }
        }
    }
}