package com.example.pass_task21

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pass_task21.ui.theme.Pass_Task21Theme
import kotlin.text.get

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Pass_Task21Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .background(Color(0xFFD3D3D3))
                    ) {
                        UnitConverterApp()
                    }
                }
            }
        }
    }
}

@Composable
fun GroupedSection(label: String, content: @Composable () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp)
                .border(BorderStroke(2.dp, Color.DarkGray), RoundedCornerShape(8.dp))
                .padding(16.dp)
        ) { content() }

        Text(
            text = label,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .background(Color(0xFFD3D3D3))
                .padding(horizontal = 8.dp),
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitConverterApp() {
    var inputValue  by remember { mutableStateOf("") }
    var outputValue by remember { mutableStateOf("") }
    var sourceUnit  by remember { mutableStateOf("$") }
    var destUnit    by remember { mutableStateOf("AUD$") }
    var sourceExpanded by remember { mutableStateOf(false) }
    var destExpanded   by remember { mutableStateOf(false) }

    // ── SOURCE dropdown: only valid INPUT units ───────────────────────────────
    val sourceDisplayUnits = listOf(
        "Currency"                   to listOf("$"),                        // USD only
        "Fuel Efficiency & Distance" to listOf("mpg", "US", "nmi"),
        "Temperature"                to listOf("Celsius", "Fahrenheit")     // Kelvin not a source
    )

    // ── Explicit allowed DESTINATION for each source unit ─────────────────────
    val allowedConversions = mapOf(
        // Currency: USD → AUD, EUR, JPY, GBP
        "$"          to listOf("AUD$", "€", "¥", "£"),
        // Fuel Efficiency & Distance (one-to-one)
        "mpg"        to listOf("km/L"),
        "US"         to listOf("Liters"),
        "nmi"        to listOf("Kilometers"),
        // Temperature: only these 3 allowed conversions
        "Celsius"    to listOf("Fahrenheit", "Kelvin"),  // C→F and C→K
        "Fahrenheit" to listOf("Celsius")                // F→C only
    )

    // ── Category label for every unit (for dropdown header display) ───────────
    val unitCategories = mapOf(
        "$"          to "Currency",                    "AUD$"       to "Currency",
        "€"          to "Currency",                    "¥"          to "Currency",
        "£"          to "Currency",
        "mpg"        to "Fuel Efficiency & Distance",  "km/L"       to "Fuel Efficiency & Distance",
        "US"         to "Fuel Efficiency & Distance",  "Liters"     to "Fuel Efficiency & Distance",
        "nmi"        to "Fuel Efficiency & Distance",  "Kilometers" to "Fuel Efficiency & Distance",
        "Celsius"    to "Temperature",                 "Fahrenheit" to "Temperature",
        "Kelvin"     to "Temperature"
    )

// ── Human-readable display labels ─────────────────────────────────────────
    val displayNames = mapOf(
        // Currency (unchanged)
        "$"          to "$",         "AUD$"       to "AUD$",
        "€"          to "€",         "¥"          to "¥",         "£" to "£",
        // Fuel Efficiency & Distance — full text + symbol
        "Liters"     to "l",
        "Kilometers" to "km",
        "Celsius"    to "°C",
        "Fahrenheit" to "°F",
        "Kelvin"     to "K"
    )

// ── Dropdown-specific labels (full text for Temperature) ──────────────────
    val dropdownNames = mapOf(
        // Currency (unchanged)
        "$"          to "$",         "AUD$"       to "AUD$",
        "€"          to "€",         "¥"          to "¥",         "£" to "£",
        // Fuel Efficiency & Distance — full text + symbol (same as displayNames)
        "mpg"        to "Mile per Gallon (mpg)",
        "km/L"       to "Kilometers per Liter (km/L)",
        "US"         to "Gallon (US)",
        "Liters"     to "Liters",
        "nmi"        to "Nautical Mile",
        "Kilometers" to "Kilometers",
        // Temperature — full text in dropdown (NOT symbols)
        "Celsius"    to "Celsius",
        "Fahrenheit" to "Fahrenheit",
        "Kelvin"     to "Kelvin"
    )


    // Destination list: only explicitly allowed units, grouped under category header
    val allowedDests = allowedConversions[sourceUnit] ?: emptyList()
    val compatibleDestUnits = allowedDests
        .groupBy { unitCategories[it] ?: "Other" }
        .map { (category, units) -> category to units }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        // ── GROUP 1: SOURCE ───────────────────────────────────────────────────
        GroupedSection(label = "SOURCE SELECTION") {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box {
                    Button(
                        onClick = { sourceExpanded = true },
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.size(50.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(displayNames[sourceUnit] ?: sourceUnit, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    DropdownMenu(
                        expanded = sourceExpanded,
                        onDismissRequest = { sourceExpanded = false }
                    ) {
                        sourceDisplayUnits.forEach { (category, items) ->
                            Text(category, modifier = Modifier.padding(8.dp), color = Color.Gray, fontSize = 11.sp)
                            items.forEach { unit ->
                                DropdownMenuItem(
                                    text = { Text(dropdownNames[unit] ?: unit) },
                                    onClick = {
                                        sourceUnit = unit
                                        sourceExpanded = false
                                        // Auto-reset destUnit to first allowed dest for new source
                                        val compatible = allowedConversions[unit] ?: emptyList()
                                        if (destUnit !in compatible) {
                                            destUnit = compatible.firstOrNull() ?: destUnit
                                        }
                                        outputValue = ""
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                OutlinedTextField(
                    value = inputValue,
                    onValueChange = { inputValue = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Input value...", color = Color.Black) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF81D4FA),
                        unfocusedContainerColor = Color(0xFF81D4FA).copy(alpha = 0.5f)
                    )
                )
            }
        }

        // ── GROUP 2: PROCESS ──────────────────────────────────────────────────
        GroupedSection(label = "PROCESS") {
            Button(
                onClick = {
                    val inputNum = inputValue.toDoubleOrNull()
                    outputValue = if (inputNum != null)
                        performConversion(inputNum, sourceUnit, destUnit)
                    else ""
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF66BB6A)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("INITIATE CONVERSION", fontWeight = FontWeight.ExtraBold)
            }
        }

        // ── GROUP 3: DESTINATION ──────────────────────────────────────────────
        GroupedSection(label = "CONVERSION RESULT") {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box {
                    Button(
                        onClick = { destExpanded = true },
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.size(50.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(displayNames[destUnit] ?: destUnit, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    DropdownMenu(
                        expanded = destExpanded,
                        onDismissRequest = { destExpanded = false }
                    ) {
                        compatibleDestUnits.forEach { (category, units) ->
                            Text(category, modifier = Modifier.padding(8.dp), color = Color.Gray, fontSize = 11.sp)
                            units.forEach { unit ->
                                DropdownMenuItem(
                                    text = { Text(dropdownNames[unit] ?: unit) },
                                    onClick = { destUnit = unit; destExpanded = false }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .background(Color(0xFF81D4FA), shape = RoundedCornerShape(4.dp))
                        .border(1.dp, Color.Gray, RoundedCornerShape(4.dp))
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(text = outputValue, color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}


// ── Conversion Logic ─────────────────────────────────────────────────────────
fun performConversion(value: Double, sourceUnit: String, destUnit: String): String {
    // 1 USD = rate units  →  formula: value / sourceRate * destRate
    val currencyRates = mapOf("$" to 1.0, "AUD$" to 1.55, "€" to 0.92, "¥" to 148.50, "£" to 0.78)

    // 1 unit = rate km/L  →  formula: value * sourceRate / destRate
    val fuelEfficiencyRates = mapOf("km/L" to 1.0, "mpg" to 0.425)

    // 1 unit = rate Liters  →  formula: value * sourceRate / destRate
    val volumeRates = mapOf("Liters" to 1.0, "US" to 3.785)

    // 1 unit = rate Kilometers  →  formula: value * sourceRate / destRate
    val distanceRates = mapOf("Kilometers" to 1.0, "nmi" to 1.852)

    return try {
        val result: Double? = when {

            // ── Temperature (non-linear) ──────────────────────────────────────
            sourceUnit == "Celsius"    && destUnit == "Fahrenheit" -> (value * 9.0 / 5.0) + 32.0
            sourceUnit == "Celsius"    && destUnit == "Kelvin"     -> value + 273.15
            sourceUnit == "Fahrenheit" && destUnit == "Celsius"    -> (value - 32.0) * 5.0 / 9.0
            sourceUnit == "Fahrenheit" && destUnit == "Kelvin"     -> (value - 32.0) * 5.0 / 9.0 + 273.15
            sourceUnit == "Kelvin"     && destUnit == "Celsius"    -> value - 273.15
            sourceUnit == "Kelvin"     && destUnit == "Fahrenheit" -> (value - 273.15) * 9.0 / 5.0 + 32.0

            // ── Currency ──────────────────────────────────────────────────────
            currencyRates.containsKey(sourceUnit) && currencyRates.containsKey(destUnit) ->
                value / (currencyRates[sourceUnit] ?: 1.0) * (currencyRates[destUnit] ?: 1.0)

            // ── Fuel Efficiency  1 mpg = 0.425 km/L ──────────────────────────
            fuelEfficiencyRates.containsKey(sourceUnit) && fuelEfficiencyRates.containsKey(destUnit) ->
                value * (fuelEfficiencyRates[sourceUnit] ?: 1.0) / (fuelEfficiencyRates[destUnit] ?: 1.0)

            // ── Volume  1 US Gallon = 3.785 Liters ───────────────────────────
            volumeRates.containsKey(sourceUnit) && volumeRates.containsKey(destUnit) ->
                value * (volumeRates[sourceUnit] ?: 1.0) / (volumeRates[destUnit] ?: 1.0)

            // ── Distance  1 nmi = 1.852 km ────────────────────────────────────
            distanceRates.containsKey(sourceUnit) && distanceRates.containsKey(destUnit) ->
                value * (distanceRates[sourceUnit] ?: 1.0) / (distanceRates[destUnit] ?: 1.0)

            sourceUnit == destUnit -> value
            else -> null
        }

        if (result == null) "Invalid Conversion" else "%.2f".format(result)

    } catch (e: Exception) { "Error" }
}
