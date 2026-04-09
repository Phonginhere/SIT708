package com.example.pass_task41.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.pass_task41.data.local.Event
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.text.style.TextAlign
import kotlinx.coroutines.delay
import androidx.compose.runtime.mutableLongStateOf


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun EventListScreen(
    events: List<Event>,
    onEditClick: (Event) -> Unit,
    onDeleteClick: (Event) -> Unit,
    onDeleteMultiple: (List<Event>) -> Unit
) {
    var selectedEvents by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val isSelecting = selectedEvents.isNotEmpty()
    var eventToDelete by remember { mutableStateOf<Event?>(null) }

    // Auto-refresh every minute to check past events
    var currentTime by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000)
            currentTime = System.currentTimeMillis()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Yellow selection bar
            if (isSelecting) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFFE65100),
                    shadowElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Delete ${selectedEvents.size}",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                        Row {
                            IconButton(onClick = { selectedEvents = emptySet() }) {
                                Text("✕", style = MaterialTheme.typography.titleLarge, color = Color.White)
                            }
                            IconButton(onClick = { showDeleteDialog = true }) {
                                Text("👍", style = MaterialTheme.typography.titleLarge)
                            }
                        }
                    }
                }
            }

            Text(
                "Upcoming event",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 8.dp),
                textAlign = TextAlign.Center
            )

            if (events.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No upcoming events", style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(events, key = { "${it.id}_$currentTime" }) { event ->
                        val isSelected = selectedEvents.contains(event.id)

                        if (isSelecting) {
                            // Selection mode — tap to toggle
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .combinedClickable(
                                        onClick = {
                                            selectedEvents = if (isSelected) {
                                                selectedEvents - event.id
                                            } else {
                                                selectedEvents + event.id
                                            }
                                        }
                                    ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) Color(0xFFFFE082) else MaterialTheme.colorScheme.surface
                                )
                            ) {
                                EventCardContent(event = event)
                            }
                        } else {
                            // Normal mode — swipe + long press to select
                            val dismissState = rememberSwipeToDismissBoxState(
                                confirmValueChange = { value ->
                                    when (value) {
                                        SwipeToDismissBoxValue.EndToStart -> {
                                            eventToDelete = event
                                            false // don't dismiss, show dialog first
                                        }
                                        SwipeToDismissBoxValue.StartToEnd -> {
                                            onEditClick(event)
                                            false
                                        }
                                        else -> false
                                    }
                                }
                            )

                            SwipeToDismissBox(
                                state = dismissState,
                                backgroundContent = {
                                    val direction = dismissState.dismissDirection
                                    val color by animateColorAsState(
                                        when (direction) {
                                            SwipeToDismissBoxValue.StartToEnd -> Color(0xFF4CAF50)
                                            SwipeToDismissBoxValue.EndToStart -> Color(0xFFF44336)
                                            else -> Color.Transparent
                                        }, label = "swipe"
                                    )
                                    val icon = when (direction) {
                                        SwipeToDismissBoxValue.StartToEnd -> Icons.Default.Edit
                                        SwipeToDismissBoxValue.EndToStart -> Icons.Default.Delete
                                        else -> Icons.Default.Delete
                                    }
                                    val alignment = when (direction) {
                                        SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                                        else -> Alignment.CenterEnd
                                    }

                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(color)
                                            .padding(horizontal = 20.dp),
                                        contentAlignment = alignment
                                    ) {
                                        Icon(icon, contentDescription = null, tint = Color.White)
                                    }
                                }
                            ) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .combinedClickable(
                                            onClick = {},
                                            onLongClick = {
                                                if (events.size >= 2) {
                                                    selectedEvents = setOf(event.id)
                                                }
                                            }
                                        ),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                                ) {
                                    EventCardContent(event = event)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Single delete confirmation dialog
        if (eventToDelete != null) {
            AlertDialog(
                onDismissRequest = { eventToDelete = null },
                title = { Text("Delete event?") },
                text = { Text("Are you sure you want to delete \"${eventToDelete!!.title}\"?") },
                confirmButton = {
                    TextButton(onClick = {
                        onDeleteClick(eventToDelete!!)
                        eventToDelete = null
                    }) {
                        Text("Yes")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        eventToDelete = null
                    }) {
                        Text("No")
                    }
                }
            )
        }

        // Delete confirmation dialog
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Delete ${selectedEvents.size} items?") },
                text = { Text("Are you sure you want to delete ${selectedEvents.size} selected events?") },
                confirmButton = {
                    TextButton(onClick = {
                        val toDelete = events.filter { selectedEvents.contains(it.id) }
                        onDeleteMultiple(toDelete)
                        selectedEvents = emptySet()
                        showDeleteDialog = false
                    }) {
                        Text("Yes")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showDeleteDialog = false
                    }) {
                        Text("No")
                    }
                }
            )
        }
    }
}

@Composable
fun EventCardContent(event: Event) {
    val sdf = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }
    val isPast = event.datetime < System.currentTimeMillis()

    Column(modifier = Modifier.padding(16.dp)) {
        Text(event.title, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(4.dp))
        Text(
            "${event.category} • ${event.location} • ${sdf.format(Date(event.datetime))}",
            style = MaterialTheme.typography.bodyMedium,
            color = if (isPast) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary
        )
        if (isPast) {
            Text(
                "Past Date",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}