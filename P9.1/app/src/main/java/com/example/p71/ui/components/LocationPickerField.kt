package com.example.p71.ui.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.material3.MenuAnchorType
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.p71.data.location.LocationProvider
import com.example.p71.data.remote.NominatimClient
import com.example.p71.data.remote.NominatimSearchResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationPickerField(
    value: String,
    latitude: Double?,
    longitude: Double?,
    onLocationChanged: (text: String, lat: Double?, lng: Double?) -> Unit,
    isError: Boolean,
    errorMessage: String?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val locationProvider = remember { LocationProvider(context) }

    var suggestions by remember { mutableStateOf<List<NominatimSearchResult>>(emptyList()) }
    var isExpanded by remember { mutableStateOf(false) }
    var isSearching by remember { mutableStateOf(false) }
    var isFetchingLocation by remember { mutableStateOf(false) }
    var skipNextSearch by remember { mutableStateOf(false) }

    suspend fun fetchAndApplyCurrentLocation() {
        isFetchingLocation = true
        try {
            val loc = locationProvider.getCurrentLocation()
            if (loc != null) {
                val text = try {
                    val reverse = NominatimClient.api.reverse(loc.latitude, loc.longitude)
                    reverse.displayName
                        ?: "%.4f, %.4f".format(loc.latitude, loc.longitude)
                } catch (e: Exception) {
                    "%.4f, %.4f".format(loc.latitude, loc.longitude)
                }
                skipNextSearch = true
                onLocationChanged(text, loc.latitude, loc.longitude)
            }
        } finally {
            isFetchingLocation = false
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) scope.launch { fetchAndApplyCurrentLocation() }
    }

    // Debounced autocomplete search
    LaunchedEffect(value) {
        if (skipNextSearch) {
            skipNextSearch = false
            return@LaunchedEffect
        }
        if (value.length < 3) {
            suggestions = emptyList()
            isExpanded = false
            return@LaunchedEffect
        }
        delay(400)
        isSearching = true
        try {
            val results = withContext(Dispatchers.IO) {
                NominatimClient.api.search(query = value)
            }
            suggestions = results
            isExpanded = results.isNotEmpty()
        } catch (e: Exception) {
            suggestions = emptyList()
            isExpanded = false
        } finally {
            isSearching = false
        }
    }

    Column(modifier = modifier) {
        ExposedDropdownMenuBox(
            expanded = isExpanded,
            onExpandedChange = { isExpanded = it && suggestions.isNotEmpty() }
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = { newText ->
                    // Free-typing clears any previously confirmed coords
                    onLocationChanged(newText, null, null)
                },
                label = { Text("Location") },
                isError = isError,
                supportingText = if (isError && errorMessage != null) {
                    { Text(errorMessage) }
                } else null,
                trailingIcon = {
                    when {
                        isSearching -> CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        latitude != null && longitude != null -> Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Location confirmed",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryEditable, true)
            )
            ExposedDropdownMenu(
                expanded = isExpanded,
                onDismissRequest = { isExpanded = false }
            ) {
                suggestions.forEach { result ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                result.displayName,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        onClick = {
                            skipNextSearch = true
                            onLocationChanged(
                                result.displayName,
                                result.latitude,
                                result.longitude
                            )
                            isExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = {
                val granted = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
                if (granted) {
                    scope.launch { fetchAndApplyCurrentLocation() }
                } else {
                    permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }
            },
            enabled = !isFetchingLocation,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isFetchingLocation) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Locating…")
            } else {
                Icon(Icons.Default.MyLocation, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Get Current Location")
            }
        }
    }
}