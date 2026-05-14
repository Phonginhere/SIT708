package com.example.p71.ui.map

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.p71.data.local.Item
import com.example.p71.data.location.LocationProvider
import com.example.p71.util.GeoUtils
import com.example.p71.viewmodel.ItemViewModel
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.FloatingActionButton
import androidx.compose.runtime.mutableIntStateOf
import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.graphics.toColorInt
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.layout.size
import org.osmdroid.library.R
import androidx.compose.animation.core.keyframes
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    viewModel: ItemViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val itemsWithCoords by viewModel.allItemsWithCoords.collectAsState()

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasPermission = granted }

    LaunchedEffect(Unit) {
        if (!hasPermission) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    val locationProvider = remember { LocationProvider(context) }
    var currentLocation by remember { mutableStateOf<Location?>(null) }

    LaunchedEffect(hasPermission) {
        if (hasPermission) {
            currentLocation = locationProvider.getCurrentLocation()
        }
    }

    var radiusKm by remember { mutableFloatStateOf(10f) }

    val visibleItems = remember(itemsWithCoords, currentLocation, radiusKm) {
        val loc = currentLocation
        if (loc == null) {
            itemsWithCoords
        } else {
            itemsWithCoords.filter { item ->
                val lat = item.latitude ?: return@filter false
                val lng = item.longitude ?: return@filter false
                GeoUtils.distanceKm(loc.latitude, loc.longitude, lat, lng) <= radiusKm
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lost & Found Map") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (!hasPermission) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Location permission is required to show the map and filter by radius.",
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = {
                    permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }) {
                    Text("Grant Permission")
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                var recenterRequest by remember { mutableIntStateOf(0) }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    OsmMapView(
                        currentLocation = currentLocation,
                        items = visibleItems,
                        radiusKm = radiusKm.toDouble(),
                        recenterTrigger = recenterRequest,
                        modifier = Modifier.fillMaxSize()
                    )

                    BouncingPin(
                        trigger = recenterRequest,
                        modifier = Modifier.fillMaxSize()
                    )

                    if (currentLocation != null) {
                        FloatingActionButton(
                            onClick = { recenterRequest++ },
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(16.dp),
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.primary
                        ) {
                            Icon(
                                imageVector = Icons.Default.MyLocation,
                                contentDescription = "Recenter on my location"
                            )
                        }
                    }
                }

                Surface(
                    tonalElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "Radius: ${radiusKm.toInt()} km",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                "${visibleItems.size} item(s) in range",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Slider(
                            value = radiusKm,
                            onValueChange = { radiusKm = it },
                            valueRange = 1f..50f,
                            steps = 48
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OsmMapView(
    currentLocation: Location?,
    items: List<Item>,
    radiusKm: Double,
    recenterTrigger: Int,
    modifier: Modifier = Modifier
) {
    val mapView = rememberMapViewWithLifecycle()
    var hasCentered by rememberSaveable { mutableStateOf(false) }

    // First-time auto-center when location becomes available
    LaunchedEffect(currentLocation) {
        currentLocation?.let { loc ->
            if (!hasCentered) {
                mapView.controller.setZoom(13.0)
                mapView.controller.setCenter(GeoPoint(loc.latitude, loc.longitude))
                hasCentered = true
            }
        }
    }

    // On-demand recenter triggered by the FAB
    LaunchedEffect(recenterTrigger) {
        if (recenterTrigger > 0) {
            currentLocation?.let { loc ->
                mapView.controller.animateTo(
                    GeoPoint(loc.latitude, loc.longitude),
                    mapView.zoomLevelDouble,
                    600L
                )
            }
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { mapView },
        update = { mv ->
            mv.overlays.clear()

            currentLocation?.let { loc ->
                val center = GeoPoint(loc.latitude, loc.longitude)

                // Blue pin for the user's current location
                mv.overlays.add(Marker(mv).apply {
                    position = center
                    title = "You are here"
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    icon = tintedPin(mv.context, "#1976D2".toColorInt())
                })

                // Radius circle (unchanged)
                mv.overlays.add(Polygon().apply {
                    points = Polygon.pointsAsCircle(center, radiusKm * 1000)
                    fillPaint.color = android.graphics.Color.argb(40, 33, 150, 243)
                    outlinePaint.color = android.graphics.Color.argb(160, 33, 150, 243)
                    outlinePaint.strokeWidth = 3f
                })
            }

            // Red for Lost items, green for Found items
            items.forEach { item ->
                val lat = item.latitude ?: return@forEach
                val lng = item.longitude ?: return@forEach
                val pinColor = if (item.postType == "Lost") {
                    "#D32F2F".toColorInt()  // red
                } else {
                    "#2E7D32".toColorInt()  // green
                }
                mv.overlays.add(Marker(mv).apply {
                    position = GeoPoint(lat, lng)
                    title = "${item.postType}: ${item.name}"
                    snippet = "${item.category} · ${item.location}"
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    icon = tintedPin(mv.context, pinColor)
                })
            }

            mv.invalidate()
        }
    )
}


@Composable
private fun BouncingPin(
    trigger: Int,
    modifier: Modifier = Modifier
) {
    val offsetY = remember { Animatable(0f) }
    var isVisible by remember { mutableStateOf(false) }
    val density = LocalDensity.current
    val pinColor = remember { Color("#1976D2".toColorInt()) }

    LaunchedEffect(trigger) {
        if (trigger > 0) {
            delay(600)  // wait for camera animation to finish
            isVisible = true
            val peakHigh = with(density) { -50.dp.toPx() }
            val peakLow = with(density) { -20.dp.toPx() }
            offsetY.snapTo(0f)
            offsetY.animateTo(
                targetValue = 0f,
                animationSpec = keyframes {
                    durationMillis = 700
                    0f at 0
                    peakHigh at 200 using FastOutSlowInEasing
                    0f at 380 using FastOutSlowInEasing
                    peakLow at 520 using FastOutSlowInEasing
                    0f at 700
                }
            )
            isVisible = false
        }
    }

    if (isVisible) {
        Box(modifier = modifier) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = pinColor,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(48.dp)
                    .graphicsLayer {
                        // Shift up by half-height so the tip sits at Box center
                        // (matching osmdroid's ANCHOR_BOTTOM), then add the bounce
                        translationY = -24.dp.toPx() + offsetY.value
                    }
            )
        }
    }
}

private fun tintedPin(context: Context, color: Int): Drawable {
    val base = ContextCompat.getDrawable(
        context,
        R.drawable.marker_default
    )!!.mutate()
    DrawableCompat.setTint(base, color)
    return base
}

@Composable
private fun rememberMapViewWithLifecycle(): MapView {
    val context = LocalContext.current
    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
        }
    }

    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                else -> Unit
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
            mapView.onDetach()
        }
    }

    return mapView
}