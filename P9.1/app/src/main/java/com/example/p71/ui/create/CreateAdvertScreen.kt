package com.example.p71.ui.create

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.MenuAnchorType
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.example.p71.ui.components.LocationPickerField
import com.example.p71.viewmodel.ItemViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import com.example.p71.ui.components.MapPreview
import androidx.compose.material3.SelectableDates
import android.content.Intent
import android.graphics.Bitmap

class TakePictureWithGrant : ActivityResultContracts.TakePicture() {
    override fun createIntent(context: Context, input: Uri): Intent {
        return super.createIntent(context, input)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            .addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAdvertScreen(
    viewModel: ItemViewModel,
    onSaveSuccess: () -> Unit
) {
    val context = LocalContext.current

    // Form state
    var postType by remember { mutableStateOf("Lost") }
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var latitude by remember { mutableStateOf<Double?>(null) }
    var longitude by remember { mutableStateOf<Double?>(null) }
    var imageBytes by remember { mutableStateOf<ByteArray?>(null) }

    // Validation errors
    var nameError by remember { mutableStateOf(false) }
    var phoneError by remember { mutableStateOf(false) }
    var descriptionError by remember { mutableStateOf(false) }
    var categoryError by remember { mutableStateOf(false) }
    var dateError by remember { mutableStateOf(false) }
    var locationError by remember { mutableStateOf(false) }
    var locationErrorMessage by remember { mutableStateOf<String?>(null) }
    var imageError by remember { mutableStateOf(false) }

    var categoryExpanded by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState(
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                utcTimeMillis <= System.currentTimeMillis()

            override fun isSelectableYear(year: Int): Boolean =
                year <= Calendar.getInstance().get(Calendar.YEAR)
        }
    )
    var showDatePicker by remember { mutableStateOf(false) }

    var cameraUri by remember { mutableStateOf<Uri?>(null) }
    var showImageDialog by remember { mutableStateOf(false) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { imageBytes = uriToByteArray(context, it) }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = TakePictureWithGrant()   // was: ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success) {
            cameraUri?.let { uri ->
                imageBytes = uriToByteArray(context, uri)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text("Create Advert", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(20.dp))

        // Post type radio buttons
        Text("Post type:", style = MaterialTheme.typography.labelLarge)
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            listOf("Lost", "Found").forEach { type ->
                Row(
                    modifier = Modifier.selectable(
                        selected = postType == type,
                        onClick = { postType = type },
                        role = Role.RadioButton
                    ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = postType == type, onClick = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(type)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Name
        OutlinedTextField(
            value = name,
            onValueChange = { name = it; nameError = false },
            label = { Text("Name") },
            isError = nameError,
            supportingText = if (nameError) {{ Text("Name is required") }} else null,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Phone
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it; phoneError = false },
            label = { Text("Phone") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            isError = phoneError,
            supportingText = if (phoneError) {{ Text("Phone is required") }} else null,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Description
        OutlinedTextField(
            value = description,
            onValueChange = { description = it; descriptionError = false },
            label = { Text("Description") },
            minLines = 3,
            isError = descriptionError,
            supportingText = if (descriptionError) {{ Text("Description is required") }} else null,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Category dropdown
        ExposedDropdownMenuBox(
            expanded = categoryExpanded,
            onExpandedChange = { categoryExpanded = it }
        ) {
            OutlinedTextField(
                value = selectedCategory,
                onValueChange = {},
                readOnly = true,
                label = { Text("Category") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                isError = categoryError,
                supportingText = if (categoryError) {{ Text("Category is required") }} else null,
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
            )
            ExposedDropdownMenu(
                expanded = categoryExpanded,
                onDismissRequest = { categoryExpanded = false }
            ) {
                viewModel.categories.forEach { category ->
                    DropdownMenuItem(
                        text = { Text(category) },
                        onClick = {
                            selectedCategory = category
                            categoryExpanded = false
                            categoryError = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Date picker
        Box {
            OutlinedTextField(
                value = date,
                onValueChange = {},
                readOnly = true,
                label = { Text("Date") },
                isError = dateError,
                supportingText = if (dateError) {{ Text("Date is required") }} else null,
                modifier = Modifier.fillMaxWidth()
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable { showDatePicker = true }
            )
        }

        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            date = sdf.format(Date(millis))
                            dateError = false
                        }
                        showDatePicker = false
                    }) { Text("OK") }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Location: autocomplete + GET CURRENT LOCATION
        LocationPickerField(
            value = location,
            latitude = latitude,
            longitude = longitude,
            onLocationChanged = { text, lat, lng ->
                location = text
                latitude = lat
                longitude = lng
                locationError = false
                locationErrorMessage = null
            },
            isError = locationError,
            errorMessage = locationErrorMessage,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Image upload
        Text("Image:", style = MaterialTheme.typography.labelLarge)

        if (latitude != null && longitude != null) {
            Spacer(modifier = Modifier.height(8.dp))
            MapPreview(
                latitude = latitude,
                longitude = longitude,
                label = location.ifBlank { "Selected location" }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (imageBytes != null) {
            val bitmap = remember(imageBytes) {
                BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes!!.size)
            }
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Selected image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { showImageDialog = true },
                    contentScale = ContentScale.Crop
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(
                        width = if (imageError) 2.dp else 1.dp,
                        color = if (imageError) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable { showImageDialog = true },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Tap to add image",
                    color = if (imageError) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (imageError) {
            Text(
                "Image is required",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }

        if (showImageDialog) {
            AlertDialog(
                onDismissRequest = { showImageDialog = false },
                title = { Text("Select Image") },
                text = { Text("Choose image source") },
                confirmButton = {
                    TextButton(onClick = {
                        showImageDialog = false
                        galleryLauncher.launch("image/*")
                    }) { Text("Gallery") }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showImageDialog = false
                        val uri = createImageUri(context)
                        cameraUri = uri
                        cameraLauncher.launch(uri)
                    }) { Text("Camera") }
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Save
        Button(
            onClick = {
                nameError = name.isBlank()
                phoneError = phone.isBlank()
                descriptionError = description.isBlank()
                categoryError = selectedCategory.isBlank()
                dateError = date.isBlank()
                imageError = imageBytes == null

                // Location validation has two failure modes
                when {
                    location.isBlank() -> {
                        locationError = true
                        locationErrorMessage = "Location is required"
                    }
                    latitude == null || longitude == null -> {
                        locationError = true
                        locationErrorMessage =
                            "Please pick a suggestion or use Get Current Location"
                    }
                    else -> {
                        locationError = false
                        locationErrorMessage = null
                    }
                }

                if (!nameError && !phoneError && !descriptionError &&
                    !categoryError && !dateError && !locationError && !imageError
                ) {
                    viewModel.insertItem(
                        postType = postType,
                        name = name,
                        phone = phone,
                        description = description,
                        category = selectedCategory,
                        date = date,
                        location = location,
                        imageData = imageBytes,
                        latitude = latitude,
                        longitude = longitude
                    )
                    onSaveSuccess()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save")
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

private fun uriToByteArray(context: Context, uri: Uri): ByteArray? {
    return try {
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            val original = BitmapFactory.decodeStream(inputStream) ?: return null

            // Scale longest edge to 1024px while preserving aspect ratio
            val maxDim = 1024
            val scaled = if (original.width > maxDim || original.height > maxDim) {
                val ratio = minOf(
                    maxDim.toFloat() / original.width,
                    maxDim.toFloat() / original.height
                )
                Bitmap.createScaledBitmap(
                    original,
                    (original.width * ratio).toInt(),
                    (original.height * ratio).toInt(),
                    true
                )
            } else {
                original
            }

            // Compress as JPEG at quality 80 — typically 100–300KB per image
            val baos = java.io.ByteArrayOutputStream()
            scaled.compress(Bitmap.CompressFormat.JPEG, 80, baos)
            baos.toByteArray()
        }
    } catch (e: Exception) {
        null
    }
}

private fun createImageUri(context: Context): Uri {
    val file = File(context.cacheDir, "camera_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        file
    )
}