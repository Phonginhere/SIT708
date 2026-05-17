package com.example.llm61.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.llm61.data.local.QuestionAttemptEntity
import com.example.llm61.data.local.QuizAttemptEntity
import com.example.llm61.viewmodel.HistoryViewModel
import com.example.llm61.viewmodel.UserViewModel
import androidx.compose.runtime.remember
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    userViewModel: UserViewModel,
    onBackClick: () -> Unit
) {
    val historyVM: HistoryViewModel = viewModel()
    val uid = userViewModel.currentUserId
    val attempts by (
            if (uid != null) historyVM.attemptsFor(uid)
            else kotlinx.coroutines.flow.flowOf(emptyList())
            ).collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("History") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (attempts.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("No quizzes yet", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Text(
                    "Take a quiz from the Home screen — your results will appear here.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(attempts, key = { it.id }) { attempt ->
                    AttemptCard(attempt = attempt, historyVM = historyVM)
                }
            }
        }
    }
}

@Composable
private fun AttemptCard(
    attempt: QuizAttemptEntity,
    historyVM: HistoryViewModel
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header — always visible
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        attempt.topic,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "${attempt.correctCount} / ${attempt.totalQuestions} correct · " +
                                formatDate(attempt.timestamp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (expanded) "Collapse" else "Expand"
                    )
                }
            }

            // Expanded detail — only collects from DB while open
            AnimatedVisibility(visible = expanded) {
                val questions by historyVM
                    .questionsFor(attempt.id)
                    .collectAsState(initial = emptyList())

                Column(modifier = Modifier.padding(top = 12.dp)) {
                    questions.forEachIndexed { index, q ->
                        QuestionDetail(number = index + 1, question = q)
                        if (index < questions.size - 1) {
                            Spacer(Modifier.height(12.dp))
                            HorizontalDivider()
                            Spacer(Modifier.height(12.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuestionDetail(number: Int, question: QuestionAttemptEntity) {
    Column {
        Text(
            "$number. ${question.questionText}",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(8.dp))
        question.options.forEachIndexed { idx, option ->
            val isCorrect = idx == question.correctIndex
            val isUserChoice = idx == question.userSelectedIndex
            val color = when {
                isCorrect -> Color(0xFF2E7D32)
                isUserChoice && !isCorrect -> MaterialTheme.colorScheme.error
                else -> MaterialTheme.colorScheme.onSurface
            }
            val suffix = when {
                isCorrect && isUserChoice -> " (Your answer — correct)"
                isCorrect -> " (Correct answer)"
                isUserChoice -> " (Your answer)"
                else -> ""
            }
            Text(
                "• $option$suffix",
                color = color,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 2.dp)
            )
        }
    }
}

private fun formatDate(epochMillis: Long): String {
    val fmt = SimpleDateFormat("d MMM, h:mm a", Locale.getDefault())
    return fmt.format(Date(epochMillis))
}