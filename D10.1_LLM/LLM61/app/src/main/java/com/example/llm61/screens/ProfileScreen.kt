package com.example.llm61.screens

import android.content.Intent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Upgrade
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.llm61.network.LlmUiState
import com.example.llm61.network.share.QrCodeGenerator
import com.example.llm61.viewmodel.ProfileViewModel
import com.example.llm61.viewmodel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userViewModel: UserViewModel,
    onBackClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onUpgradeClick: () -> Unit
) {
    val profileVM: ProfileViewModel = viewModel()
    val uid = userViewModel.currentUserId
    if (uid == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }
    val totalQuestions by profileVM.totalQuestions(uid).collectAsState(initial = 0)
    val totalCorrect by profileVM.totalCorrect(uid).collectAsState(initial = 0)
    val totalIncorrect by profileVM.totalIncorrect(uid).collectAsState(initial = 0)
    val aiSummaryState = profileVM.aiSummaryState
    var showShareDialog by remember { mutableStateOf(false) }

    if (showShareDialog) {
        ShareProfileDialog(
            username = userViewModel.username.ifBlank { "Student" },
            tier = userViewModel.currentTier,
            totalQuestions = totalQuestions,
            totalCorrect = totalCorrect,
            interests = userViewModel.selectedInterests,
            onDismiss = { showShareDialog = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Button(
                    onClick = { showShareDialog = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(16.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Share Profile")
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
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            userViewModel.username.ifBlank { "Student" },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(4.dp))
                        TierBadge(tier = userViewModel.currentTier)
                        if (userViewModel.email.isNotBlank()) {
                            Text(
                                userViewModel.email,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatCard("Total Questions", totalQuestions.toString(), Modifier.weight(1f))
                StatCard("Correctly Answered", totalCorrect.toString(), Modifier.weight(1f))
                StatCard("Incorrect Answers", totalIncorrect.toString(), Modifier.weight(1f))
            }

            Spacer(Modifier.height(16.dp))

            AiSummarySection(
                state = aiSummaryState,
                onRequestSummary = { profileVM.requestAiSummary(uid) },
                onClearSummary = { profileVM.clearAiSummary() }
            )

            Spacer(Modifier.height(16.dp))

            Card(modifier = Modifier.fillMaxWidth()) {
                Column {
                    ListItem(
                        headlineContent = { Text("Quiz History") },
                        supportingContent = { Text("Review your past quizzes") },
                        leadingContent = { Icon(Icons.Default.History, contentDescription = null) },
                        modifier = Modifier.clickable { onHistoryClick() }
                    )
                    HorizontalDivider()
                    ListItem(
                        headlineContent = { Text("Upgrade Account") },
                        supportingContent = { Text("Unlock improved quiz generation") },
                        leadingContent = { Icon(Icons.Default.Upgrade, contentDescription = null) },
                        modifier = Modifier.clickable { onUpgradeClick() }
                    )
                }
            }

            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            Text(
                value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun AiSummarySection(
    state: LlmUiState,
    onRequestSummary: () -> Unit,
    onClearSummary: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Summarized by AI",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                "Query the LLM to get a summary of your incorrect answers.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))

            when (state) {
                LlmUiState.Idle -> {
                    OutlinedButton(
                        onClick = onRequestSummary,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Summarize my weak areas")
                    }
                }
                LlmUiState.Loading -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(8.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.width(12.dp))
                        Text("Analyzing your history...")
                    }
                }
                is LlmUiState.Success -> {
                    Column {
                        PromptAndResponseDisplay(
                            prompt = state.prompt,
                            response = state.response,
                            responseLabel = "🎯 Your Focus Areas",
                            responseColor = MaterialTheme.colorScheme.primaryContainer
                        )
                        TextButton(onClick = onClearSummary) {
                            Text("Hide summary")
                        }
                    }
                }
                is LlmUiState.Error -> {
                    Column {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    "❌ Couldn't generate summary",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(state.message, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = onRequestSummary,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Try Again")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TierBadge(tier: String) {
    val (color, label) = when (tier) {
        "starter" -> Color(0xFF1976D2) to "Starter Plan"
        "intermediate" -> Color(0xFF388E3C) to "Intermediate ⭐"
        "advanced" -> Color(0xFFFFB300) to "Advanced 👑"
        else -> MaterialTheme.colorScheme.outline to "Free Plan"
    }
    Surface(
        color = color,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ShareProfileDialog(
    username: String,
    tier: String,
    totalQuestions: Int,
    totalCorrect: Int,
    interests: Set<String>,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val accuracy = if (totalQuestions > 0) (totalCorrect * 100) / totalQuestions else 0

    val tierDisplay = when (tier) {
        "starter" -> "Starter"
        "intermediate" -> "Intermediate ⭐"
        "advanced" -> "Advanced 👑"
        else -> "Free"
    }
    val interestsLine = if (interests.isEmpty()) "None selected"
    else interests.joinToString(", ")

    val shareText = buildString {
        appendLine("📚 LLM61 Learning Profile")
        appendLine()
        appendLine("👤 $username")
        appendLine("🎖️ Tier: $tierDisplay")
        if (totalQuestions > 0) {
            appendLine("📊 $totalCorrect / $totalQuestions correct ($accuracy%)")
        }
        appendLine()
        append("📌 Interests: $interestsLine")
    }

    val deepLinkUri = remember(username, tier, totalQuestions, totalCorrect, interests) {
        QrCodeGenerator.buildProfileShareUri(
            username = username,
            tier = tier,
            totalQuestions = totalQuestions,
            totalCorrect = totalCorrect,
            interests = interests
        )
    }
    val qrBitmap = remember(deepLinkUri) {
        QrCodeGenerator.generateBitmap(deepLinkUri, 512)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Share Profile") },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                Image(
                    bitmap = qrBitmap.asImageBitmap(),
                    contentDescription = "Profile QR Code",
                    modifier = Modifier.size(220.dp)
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Scan with another phone to view this profile",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(Modifier.height(8.dp))
                Text(shareText, style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = {
                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, shareText)
                        putExtra(Intent.EXTRA_SUBJECT, "My LLM61 Profile")
                    }
                    context.startActivity(Intent.createChooser(sendIntent, "Share Profile"))
                    onDismiss()
                }) { Text("Share Text") }

                Button(onClick = {
                    val uri = QrCodeGenerator.saveBitmapToCache(context, qrBitmap)
                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "image/png"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        putExtra(Intent.EXTRA_TEXT, shareText)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(sendIntent, "Share Profile QR"))
                    onDismiss()
                }) { Text("Share QR") }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}