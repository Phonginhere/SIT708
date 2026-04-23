package com.example.llm61.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.llm61.viewmodel.UserViewModel

// All possible topics for random task generation
private val allTopics = listOf(
    "Algorithms", "Data Structures", "Web Development", "Testing",
    "Machine Learning", "Databases", "Operating Systems", "Networking",
    "Mobile Development", "Cloud Computing", "Security", "DevOps",
    "Game Development", "AI", "UI/UX Design"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    userViewModel: UserViewModel,
    onTaskClick: (String) -> Unit,
    onEditInterestsClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    // Decide which topics to use for tasks:
    // - If user picked interests → use those
    // - Otherwise → pick 3 random topics
    val selectedInterests = userViewModel.selectedInterests
    val taskTopics = remember(selectedInterests) {
        if (selectedInterests.isNotEmpty()) {
            selectedInterests.shuffled().take(3)
        } else {
            allTopics.shuffled().take(3)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Hello,",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            userViewModel.username.ifBlank { "Student" },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onEditInterestsClick) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit Interests"
                        )
                    }
                    TextButton(onClick = onLogoutClick) { Text("Logout") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "You have ${taskTopics.size} task(s) due",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Spacer(Modifier.height(8.dp))

            if (selectedInterests.isEmpty()) {
                Text(
                    "Showing random topics. Tap ✏️ above to pick your interests.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(16.dp))

            // List of generated tasks — passes the TOPIC name, not "task_1"
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(taskTopics) { topic ->
                    TaskCard(
                        topic = topic,
                        onClick = { onTaskClick(topic) }   // ← passes real topic
                    )
                }
            }
        }
    }
}

@Composable
private fun TaskCard(topic: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "✨ Generated Task",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    topic,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "A short quiz on $topic to test your knowledge.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            IconButton(onClick = onClick) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = "Start task",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}