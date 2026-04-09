package com.example.pass_task41.ui

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.pass_task41.data.local.Event
import java.text.SimpleDateFormat
import java.util.*
import kotlin.text.category
import kotlin.text.format
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.heightIn
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState

private suspend fun searchLocation(query: String): List<String> {
    return withContext(Dispatchers.IO) {
        try {
            val client = OkHttpClient()
            val url = "https://nominatim.openstreetmap.org/search?q=${
                java.net.URLEncoder.encode(query, "UTF-8")
            }&format=json&limit=5"

            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "EventPlannerApp/1.0")
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: return@withContext emptyList()
            val jsonArray = JSONArray(body)

            val results = mutableListOf<String>()
            for (i in 0 until jsonArray.length()) {
                val item = jsonArray.getJSONObject(i)
                results.add(item.getString("display_name"))
            }
            results
        } catch (e: Exception) {
            emptyList()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditEventScreen(
    existingEvent: Event?,
    fixedCategory: String?,
    onSave: (Event) -> Unit,
    onBack: () -> Unit,
    onCategoryChanged: (String) -> Unit = {},
    onHasChanges: (Boolean) -> Unit = {},
    externalShowExitDialog: MutableState<Boolean>? = null
) {
    val context = LocalContext.current
    val sdf = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }

    val eventKey = existingEvent?.id

    var title by remember(eventKey) { mutableStateOf(existingEvent?.title ?: "") }
    var location by remember(eventKey) { mutableStateOf(existingEvent?.location ?: "") }
    var selectedCategory by remember(eventKey) {
        mutableStateOf(existingEvent?.category ?: fixedCategory ?: "Event")
    }
    var dateTimeMillis by remember(eventKey) { mutableLongStateOf(existingEvent?.datetime ?: 0L) }
    var dateTimeText by remember(eventKey) {
        mutableStateOf(existingEvent?.let { sdf.format(Date(it.datetime)) } ?: "")
    }

    var expanded by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val categories = listOf("Event", "Trip", "Appointment")

    val isEditing = existingEvent != null
    val isCategoryEditable = isEditing

    var titleError by remember { mutableStateOf<String?>(null) }
    var dateError by remember { mutableStateOf<String?>(null) }

    var showExitDialog by remember { mutableStateOf(false) }

    // Sync with external trigger
    LaunchedEffect(externalShowExitDialog?.value) {
        if (externalShowExitDialog?.value == true) {
            showExitDialog = true
            externalShowExitDialog.value = false
        }
    }

    // Track if user changed anything
    val hasChanges = remember(title, location, selectedCategory, dateTimeMillis) {
        if (existingEvent != null) {
            title != existingEvent.title ||
                    location != existingEvent.location ||
                    selectedCategory != existingEvent.category ||
                    (dateTimeMillis != 0L && dateTimeMillis != existingEvent.datetime)
        } else {
            title.isNotBlank() || location.isNotBlank() || dateTimeMillis != 0L
        }
    }

    LaunchedEffect(hasChanges) {
        onHasChanges(hasChanges)
    }

    // Date/Time picker logic
    fun showDateTimePicker() {
        val cal = Calendar.getInstance()
        val datePicker = DatePickerDialog(context, { _, y, m, d ->
            TimePickerDialog(context, { _, h, min ->
                cal.set(y, m, d, h, min, 0)
                if (cal.timeInMillis < System.currentTimeMillis()) {
                    dateError = "Cannot select a date in the past"
                } else {
                    dateTimeMillis = cal.timeInMillis
                    dateTimeText = sdf.format(cal.time)
                    dateError = null
                }
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), false).show()
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))

        datePicker.datePicker.minDate = System.currentTimeMillis()
        datePicker.show()
    }

    BackHandler(enabled = hasChanges) {
        showExitDialog = true
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(if (isEditing) "Edit $selectedCategory" else "New $selectedCategory") })
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // Exit confirmation dialog
            if (showExitDialog) {
                AlertDialog(
                    onDismissRequest = { showExitDialog = false },
                    title = { Text("Unsaved changes") },
                    text = { Text("Do you want to save your changes before leaving?") },
                    confirmButton = {
                        TextButton(onClick = {
                            // Validate before saving
                            titleError = if (title.isBlank()) "Title cannot be empty" else null
                            dateError = when {
                                dateTimeMillis == 0L -> "Please select a date and time"
                                dateTimeMillis < System.currentTimeMillis() -> "Cannot select a date in the past"
                                else -> null
                            }

                            if (titleError == null && dateError == null) {
                                onSave(
                                    Event(
                                        id = existingEvent?.id ?: 0,
                                        title = title.trim(),
                                        category = selectedCategory,
                                        location = location.trim(),
                                        datetime = dateTimeMillis
                                    )
                                )
                            }
                            showExitDialog = false
                        }) {
                            Text("Yes")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            showExitDialog = false
                            onBack()
                        }) {
                            Text("No")
                        }
                    }
                )
            }

            // Title
            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it
                    if (it.isNotBlank()) titleError = null
                },
                label = { Text("Title *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = titleError != null,
                supportingText = if (titleError != null) {
                    { Text(titleError!!, color = MaterialTheme.colorScheme.error) }
                } else null
            )

            // Category
            if (isCategoryEditable) {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    selectedCategory = cat
                                    expanded = false
                                    onCategoryChanged(cat)
                                }
                            )
                        }
                    }
                }
            } else {
                OutlinedTextField(
                    value = selectedCategory,
                    onValueChange = {},
                    label = { Text("Category") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    enabled = false
                )
            }

            // Location
            // Location with autocomplete
            var locationSuggestions by remember { mutableStateOf<List<String>>(emptyList()) }
            var showSuggestions by remember { mutableStateOf(false) }
            val scope = rememberCoroutineScope()
            var searchJob by remember { mutableStateOf<Job?>(null) }

            Box(modifier = Modifier.fillMaxWidth()) {
                Column {
                    OutlinedTextField(
                        value = location,
                        onValueChange = { query ->
                            location = query
                            searchJob?.cancel()
                            if (query.length >= 3) {
                                searchJob = scope.launch {
                                    delay(500) // debounce
                                    val results = searchLocation(query)
                                    locationSuggestions = results
                                    showSuggestions = results.isNotEmpty()
                                }
                            } else {
                                locationSuggestions = emptyList()
                                showSuggestions = false
                            }
                        },
                        label = { Text("Location") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    if (showSuggestions) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 200.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier.verticalScroll(rememberScrollState())
                            ) {
                                TextButton(
                                    onClick = { showSuggestions = false },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Close", color = MaterialTheme.colorScheme.error)
                                }
                                locationSuggestions.forEach { suggestion ->
                                    TextButton(
                                        onClick = {
                                            location = suggestion
                                            showSuggestions = false
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            suggestion,
                                            modifier = Modifier.fillMaxWidth(),
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Date/Time picker
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = dateTimeText,
                    onValueChange = {},
                    label = { Text("Date / Time *") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    isError = dateError != null,
                    supportingText = if (dateError != null) {
                        { Text(dateError!!, color = MaterialTheme.colorScheme.error) }
                    } else null
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable {
                            showDateTimePicker()
                            dateError = null
                        }
                )
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    titleError = if (title.isBlank()) "Title cannot be empty" else null
                    dateError = when {
                        dateTimeMillis == 0L -> "Please select a date and time"
                        dateTimeMillis < System.currentTimeMillis() -> "Cannot select a date in the past"
                        else -> null
                    }

                    if (titleError == null && dateError == null) {
                        onSave(
                            Event(
                                id = existingEvent?.id ?: 0,
                                title = title.trim(),
                                category = selectedCategory,
                                location = location.trim(),
                                datetime = dateTimeMillis
                            )
                        )
                    } else {
                        val errors = listOfNotNull(titleError, dateError)
                        scope.launch {
                            snackbarHostState.showSnackbar(errors.joinToString(" • "))
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isEditing) "Update" else "Submit")
            }
        }
    }
}