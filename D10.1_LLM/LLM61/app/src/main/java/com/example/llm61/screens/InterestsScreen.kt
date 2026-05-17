package com.example.llm61.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.llm61.viewmodel.UserViewModel

// All the topics the user can pick from. Match the ones from the mock-up + a few more.
private val availableTopics = listOf(
    "Algorithms", "Data Structures", "Web Development", "Testing",
    "Machine Learning", "Databases", "Operating Systems", "Networking",
    "Mobile Development", "Cloud Computing", "Security", "DevOps",
    "Game Development", "AI", "UI/UX Design"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InterestsScreen(
    userViewModel: UserViewModel,
    fromHome: Boolean,
    onNextClick: () -> Unit,
    onBackClick: () -> Unit
) {
    // Read selected interests directly from the ViewModel so the UI updates
    val selected = userViewModel.selectedInterests

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Your Interests") },
                navigationIcon = {
                    if (fromHome) {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    } else {
                        TextButton(onClick = onBackClick) {
                            Text("Skip")
                        }
                    }
                }
            )
        },
        bottomBar = {
            // Bottom bar with the Next button — disabled until at least 1 selected
            Surface(shadowElevation = 8.dp) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "${selected.size} of 10 selected",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Button(
                        onClick = onNextClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary
                        ),
                        enabled = selected.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Next")
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = "You may select up to 10 topics",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Grid of selectable topic chips, 2 per row
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(availableTopics) { topic ->
                    val isSelected = topic in selected
                    FilterChip(
                        selected = isSelected,
                        onClick = { userViewModel.toggleInterest(topic) },
                        label = { Text(topic) },
                        leadingIcon = if (isSelected) {
                            { Icon(Icons.Default.Check, contentDescription = null) }
                        } else null,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}